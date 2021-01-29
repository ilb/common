# common-jaxrs
Various jaxrs support classes: XmlAdapters, ParamConverterProviders for core java classes

## Changelog

### common-1.36
added UUIDConverterUniversal (multi-database uuid converter)

### common-1.35
added common-lock module with ReadWriteLockFactory and StampedLockFactory, LockedExecutor

### common-1.34
removed obsolete MOXyJsonProvider, stock org.eclipse.persistence.jaxb.rs.MOXyJsonProvider should be used
with eclipselink v2.7.5+

### common 1.31
introduce common-springboot to simplify auto-registration of frequently used beans

### common-1.27
1. Auto-configuration of SendMailExceptionHandler from JNDI/System properties/Environment variable SERVER_ADMIN. No more manual configuration required. Automatic @Provider registration.
2. Support for cascade jax-rs exceptions in WebApplicationExceptionHandler (hide and log message, issue HTTP 555 error to client). To catch cascade jax-rs client exceptions, set property
  &lt;entry key="support.wae.spec.optimization" value="false"/>
3. Remove BadExceptionHandler (WebApplicationExceptionHandler should be enough)
