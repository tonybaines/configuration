package com.github.tonybaines.gestalt.validation

import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.Configurations

import static com.github.tonybaines.gestalt.Configurations.Utils.declaredMethodsOf
import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

class ReflectionValidator {
  private final Object instance
  private final Class configInterface
  private failures = new ValidationResult()

  public ReflectionValidator(Object instance, Class configInterface) {
    this.configInterface = configInterface
    this.instance = instance
  }

  ValidationResult validate() {
    failures = new ValidationResult()
    recursiveValidation(instance, configInterface)
    failures
  }

  private def recursiveValidation(object, configInterface, pathSoFar = "") {
    declaredMethodsOf(configInterface).each { method ->
      String propertyName = Configurations.Utils.fromBeanSpec(method.name)
      try {

        if (method.returnType.equals(ValidationResult.class) || method.returnType.equals(ValidationResult.Item.class)) {
          failures << object.invokeMethod(method.name, null)
          return
        }
        
        if (!object.hasProperty(propertyName)) return

        def value = object."${propertyName}"

        // Simple values, enums and custom types
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || hasAFromStringMethod(method.returnType) ) return

        // Lists of values
        if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]

          if (!Configurations.Utils.isAValueType(listGenericType)) {
            // A list of sub-types
            value.each { item -> recursiveValidation(item, listGenericType, fullPath(pathSoFar, propertyName)) }
          }
        }
        // a single sub-type
        else {
          if (value != null) recursiveValidation(value, method.returnType, fullPath(pathSoFar, propertyName))
        }
      } catch (ConfigurationException e) {
        failures << new ValidationResult.Item(fullPath(pathSoFar, propertyName), (e?.cause?.message ?: "Is undefined."), Configurations.Utils.annotationInfo(method))
      }
    }
  }

  private static fullPath(pathSoFar, propertyName) {
    (pathSoFar.isEmpty() ? '' : "${pathSoFar}.") + "${propertyName}"
  }

}
