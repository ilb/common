# common-jaxrs
Various jaxrs support classes: XmlAdapters, ParamConverterProviders for core java classes

## Changelog

### common-1.27
1. Auto-configuration of SendMailExceptionHandler from JNDI/System properties/Environment variable SERVER_ADMIN. No more manual configuration required. Automatic @Provider registration.
2. Support for cascade jax-rs exceptions in WebApplicationExceptionHandler (hide and log message, issue HTTP 555 error to client)
3. Remove BadExceptionHandler (WebApplicationExceptionHandler should be enough)
