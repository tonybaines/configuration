package com.github.tonybaines.gestalt

import spock.lang.Specification

class ConcreteTypeSpec extends Specification {

  def "Can create a config instance containing a concrete type"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'

    when:
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    then:
    configInstance.getConcrete().toString() == 'foo'

  }

  def "Can serialise a config instance with a concrete type to Properties"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    when:
    Properties serialisedProps = Configurations.toProperties(configInstance, ConfigWithAConcreteType)

    then:
    serialisedProps.'concrete' == 'foo'

  }

  def "Can serialise a config instance with a concrete type to XML"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    when:
    String xmlString = Configurations.toXml(configInstance, ConfigWithAConcreteType)
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.concrete == 'foo'

  }

  interface ConfigWithAConcreteType {
    ConcreteType getConcrete()
  }

  static class ConcreteType {
    private String value

    /** Factory method */
    public static ConcreteType fromString(String value) {
      return new ConcreteType(value)
    }

    private ConcreteType(String value) {
      this.value = value
    }

    @Override
    String toString() {
      value
    }
  }
}