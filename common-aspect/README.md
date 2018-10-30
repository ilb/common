# common-aspect
Class for make anotated control under method with self logger
Needs for tomcat schedulled tasks.

Uses:
in beans.xml write
<aop:aspectj-autoproxy/> <!-- AspectJ beans config -->
<bean class="ru.ilb.common.aspect.autoconfig.UnderLoggerControlConfig"/> <!-- This logger bean config -->

in your class:
make annotation (examples):
1.
@UnderLoggerControl(loggerClass = ZabbixStateLogger.class, controller = "ImportDeposits", autoAdminExceptionNotification = false)
before method what needs controls.

in class ZabbixStateLogger (thats based on BaseStateLogger) writen methods start(), work(), end()
You can write self logger.
autoAdminExceptionNotification (def=true) - if in controlled method throwing exception, automatically send email to server admin

2. 
@UnderLoggerControl(controller = "ImportDocuments")

Only reports to server admin about exceptions in method

