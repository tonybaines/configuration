package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method


class CompositeConfiguration<T> implements Configurations.Configuration<T> {
  private final List<Configurations.Configuration<T>> configurations
  private final Class configInterface

  CompositeConfiguration(Class configInterface, List<Configurations.Configuration<T>> configurations) {
    this.configInterface = configInterface
    this.configurations = configurations
  }

  @Override
  T load() {
    return new CompositeConfigurationProxy(configurations.collect { it.load() }).around(configInterface) as T
  }

  static class CompositeConfigurationProxy<T> implements InvocationHandler {
    private final List<T> configs

    CompositeConfigurationProxy(configs) {
      this.configs = configs
    }

    @Override
    public def around(Class configInterface) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], this)
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      tryAll(method, configs)
    }

    private Object tryAll(Method method, List<T> configs) {
      if (configs.empty) throw new ConfigurationException(method.name, "not found in any source")
      try {
        def config = configs.head()
        return config."${Configurations.fromBeanSpec(method.name)}"
      }
      catch (Exception e) {
        if (configs.tail().empty) throw e
        else return tryAll(method, configs.tail())
      }
    }
  }
}
