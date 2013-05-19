# Confluence Mail to News Plugin

Post news entries using email. 
This is a simple plugin which can monitor an IMAP/S or POP3/S mailbox and publish emails as news entries. 
Special about it is that the space, in which the news should be published can be defined as an email-address wildcard (email+spacekey@domain.net).

## Changes to the original plug-in
### Architecture
Some refactorings for easier debugging and a few unit tests.

### Identifying spaces
In addition to the original mail2news plug-in, this fork s able to identify the space key by
* e-mail address ("wiki+$SPACEKEY@domain.tld")
* or subject of e-mail ("[$SPACEKEY]" or "$SPACEKEY: some title")
 
### Sharing with others
Confluence 5 introduced the functionality of sharing pages. This plug-in allows you to share a blog post after it is published.
Every user to whom the blog post shall be published must be mentioned in the "Share-With:" header of the e-mail. 

This use case might be very specific to our environment.
- I use the sharing option of Android/Chrome to share the blog post via e-mail (To: wiki@our-site.de). Every user I want to inform about the page I am currently visiting is put inside the CC field.
- Our Exchange server receives the e-mail and executes the Generic Exchange Transport Agent (GETA, https://github.com/NeosIT/generic-exchange-transport-agent, kudos to @prunkstar)
- A specific GETA plug-in moves the additional recipients from the To/CC mail header to a new "Share-With" header. This prevents that duplicate e-mails will be sent to recipients. The approach ensures that the information base is inside Confluence and that further discussions will be handled through a central platform.
- Exchange moves the transformed e-mail to the Wiki inbox
- Confluence / news2mail checks the inbox and shares the created blog post based on the Share-With header
- Users receive e-mail with link to blog post   
 
 ## Hacking
 func-test.jar and share-pages.jar can not be downloaded with the current pom.xml. I will fix this later. For the meantime, download the JARs manually into your .m2/repository directory.