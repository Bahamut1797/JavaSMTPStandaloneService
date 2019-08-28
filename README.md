# Java SMTP Standalone Service

This SMTP Service was made as an alternative use of local SMTP commands, like when your application server hasn't commands to execute a SMTP service or you don't had permission to install 3rd party software.


## How to run

This is a simple Java Standalone application, so you only need Java 1.7+ installed on your environment to execute it. The command to do it is the next:

	java -jar <pathToJar>/JavaSMTPService.jar

By default the jar search for the default configuration file called "defaultSMTP.conf", to create it use the next command and set all information needed to call a SMTP Service:

	java -jar <pathToJar>/JavaSMTPService.jar configure
	
In case you want to use your own configuration file you have to send the path of the file ypu want to use:

	java -jar <pathToJar>/JavaSMTPService.jar /path/to/configure/file.conf

## Make a Configure File

If you don't want to use the command to make a configure file, you have to respect the next structure of the file:

- SMTP_HOST PORT
- TO\_EMAIL,TO\_EMAIL2,...``<space>``[TO\_EMAIL\_CC,TO\_EMAIL\_CC2,...]``<space>``[TO\_EMAIL\_BCC,TO\_EMAIL\_BCC2,...]
- FROM_EMAIL PASSWORD
- TLS|SSL|NONE <= Only one of this options
- FROM_NAME
- SUBJECT
- BODY_MESSAGE
- [ATTACHMENT,ATTACHMENT2,...]

**NOTE:** Attributes in brackets **[ ]** are optional.

## How it works

You need to execute the jar from the application you need to use a SMTP service, the jar will read the configuration file and invoke a SMTP service inside and send the email with the registered information inside the file. The jar will return an exit code 0 if email was successfully send it or an error code and message error like this:

- 101 - File <fileName> not found.
- 102 - I/O Error.
- 103 - Mail not sent + error msg.
