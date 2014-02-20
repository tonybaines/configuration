package tonybaines.configuration

import java.beans.Introspector

class Configurations<T> {
  static Configurations.Configuration.Factory<T> definedBy(Class configInterface) {
    new Configurations.Configuration.Factory(configInterface)
  }

  static String fromBeanSpec(String methodName) {
    Introspector.decapitalize(methodName.replace('get', ''))
  }

  public static interface Configuration<T> {

    T load()

    static class Factory<T> {
      Class configInterface

      Factory(configInterface) {
        this.configInterface = configInterface
      }

      public <T> Configuration<T> fromXmlFile(String filePath) {
        new XmlConfiguration(configInterface, filePath)
      }

      public <T> Configuration<T> fromPropertiesFile(String filePath) {
        new PropertiesConfiguration(configInterface, filePath)
      }

      public <T> Configuration<T> fromGroovyConfigFile(String filePath) {
        new GroovyConfigConfiguration(configInterface, filePath)
      }

      public <T> Configuration<T> fromDefaults() {
        new DefaultConfiguration(configInterface)
      }

      public CompositeConfigurationBuilder composedOf() {
        new CompositeConfigurationBuilder()
      }

      class CompositeConfigurationBuilder {
        List<Configuration<T>> strategies = new ArrayList<>()

        public CompositeConfigurationBuilder thenFallbackToDefaults() {
          strategies << Factory.this.fromDefaults()
          this
        }

        public CompositeConfigurationBuilder fromXmlFile(String filePath) {
          strategies << Factory.this.fromXmlFile(filePath)
          this
        }

        public CompositeConfigurationBuilder fromPropertiesFile(String filePath) {
          strategies << Factory.this.fromPropertiesFile(filePath)
          this
        }

        public CompositeConfigurationBuilder fromGroovyConfigFile(String filePath) {
          strategies << Factory.this.fromGroovyConfigFile(filePath)
          this
        }


        public <T> Configuration<T> done() {
          new CompositeConfiguration<T>(configInterface, strategies)
        }
      }

    }
  }
}