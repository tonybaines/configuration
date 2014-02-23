package tonybaines.configuration.sources

import tonybaines.configuration.ConfigSource
import tonybaines.configuration.ConfigurationException

import java.lang.reflect.Method

class CompositeConfigSource<T> implements ConfigSource {
  private final List<ConfigSource> sources

  CompositeConfigSource(List<T> sources) {
    this.sources = sources
  }

  @Override
  def lookup(List<String> path, Method method) {
    return tryAll(path, method, sources)
  }

  def tryAll(List<String> path, Method method, List<ConfigSource> remainingSources) {
    if (remainingSources.empty) throw new ConfigurationException(method.name, "not found in any source")
    def value = remainingSources.head().lookup(path, method)
    if (value != null) return value
    else return tryAll(path, method, remainingSources.tail())
  }
}