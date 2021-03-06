package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.validation.ValidationResult
import com.google.common.collect.Lists
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.constraints.NotNull

import static com.github.tonybaines.gestalt.Fixture.newCompositeConfiguration

class ValidationSpec extends Specification {

    def "If the default value is used and it breaks validation constraints, it is ignored (null returned)"() {
        given:
        TestConfig configuration = newCompositeConfiguration()

        when:
        configuration.getStringValueWhoseDefaultBreaksValidation()

        then:
        def e = thrown(ConfigurationException)
        e.message.contains('Failed to handle getStringValueWhoseDefaultBreaksValidation')
    }

    def "If the default value is used and it breaks validation constraints, it is ignored (null returned) in a sub-interface"() {
        given:
        TestConfig configuration = newCompositeConfiguration()

        when:
        configuration.getStringValueWhoseDefaultBreaksValidation()

        then:
        def e = thrown(ConfigurationException)
        e.message.contains('Failed to handle getStringValueWhoseDefaultBreaksValidation')
    }

    def "An invalid configured value in all sources will fall-back to a default"() {
        given:
        TestConfig configuration = newCompositeConfiguration()

        expect:
        configuration.getSubConfig().getValueWhichIsDefinedToBreakValidationButHasADefault() == "fin"
    }

    def "Validating an entire instance"() {
        given:
        TestConfig configuration = Configurations.definedBy(TestConfig)
                .fromPropertiesResource('common.properties')
                .done()

        when:
        ValidationResult validationResult = Configurations.validate(configuration, TestConfig)
        println validationResult

        then:
        validationResult.hasFailures()
        validationResult.any { it.property == 'stringValueWhoseDefaultBreaksValidation' }
        validationResult.any { it.property == 'integerThatIsTooLarge' }
        validationResult.any { it.property == 'subConfig.booleanValueWhoseValueBreaksValidation' }
        validationResult.any { it.property == 'subConfig.l2.nonExistent' }
        validationResult.any { it.property == 'custom-validation' && it.message == 'foobar' }
    }

    @Unroll
    def "Issue #7: Validating an instance where a mandatory value is null for #configInterface"() {
        when:
        ValidationResult validationResult =
                Configurations.validate(Configurations.definedBy(configInterface).fromProperties(new Properties()).done(),
                        configInterface)
        println validationResult

        then:
        validationResult.hasFailures()

        where:
        configInterface               | _
        Wrapper                       | _
        ValidatingANullMandatoryValue | _

    }

    def "Issue 19: Validating a custom type"() {
        given:
        CustomType config = Configurations.definedBy(CustomType).fromProperties(new Properties()).done()
        when:
        ValidationResult validationResult = Configurations.validate(config, CustomType)

        then:
        !validationResult.hasFailures()
    }

    def "Validating with a custom validator (multiple validations)"() {
        def properties = new Properties()
        properties.put('foo', 'foo')
        properties.put('bar', 'bar')
        given:
        CustomValidationConfig config = Configurations.definedBy(CustomValidationConfig).fromProperties(properties).done()
        when:
        ValidationResult validationResult = Configurations.validate(config, CustomValidationConfig)

        then:
        validationResult.hasFailures()
        validationResult.items.size() == 2
        validationResult.items[0].toString().contains('Only Foo *or* Bar should be defined')
    }

    def "Validating with a custom validator (single validation)"() {
        def properties = new Properties()
        properties.put('foo', 'baz')
        properties.put('baz', 'baz')
        given:
        CustomValidationConfig config = Configurations.definedBy(CustomValidationConfig).fromProperties(properties).done()
        when:
        ValidationResult validationResult = Configurations.validate(config, CustomValidationConfig)

        then:
        validationResult.hasFailures()
        validationResult.items.size() == 1
        validationResult.items[0].toString().contains("foo cannot be 'baz' if baz is also 'baz'")
    }

    interface CustomType {
        @Default.String("foo")
        @NotNull
        ObfuscatedString getObfuscatedString()
    }

    interface Wrapper {
        public ValidatingANullMandatoryValue getSubLevel()
    }

    interface ValidatingANullMandatoryValue {
        @NotNull
        public Integer getPort()
    }


}