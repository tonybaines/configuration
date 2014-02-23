package tonybaines.configuration

import spock.lang.Ignore
import spock.lang.Specification

import static tonybaines.configuration.Configurations.Feature.Defaults
import static tonybaines.configuration.Configurations.Feature.Validation
import static tonybaines.configuration.Fixture.newCompositeConfigurationBuilder

class ConfiguringConfigurationSpec extends Specification {

  def "Validation can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(Validation).done()

    expect:
    config.getSubConfig().getValueWhichIsDefinedToBreakValidationButHasADefault() == 'props'
  }

  def "Using defaults can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(Defaults).done()

    when:
    config.getNonExistentBooleanWithDefault()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getNonExistentBooleanWithDefault')
  }

  @Ignore
  def "Exceptions on missing lookups can be switched-off"() {}

  @Ignore
  def "Caching can be switched-off"() {}
}