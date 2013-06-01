# Confluence Mail to News Plugin
Post news entries/blog posts using e-mail. 
This is a simple plugin which can monitor an IMAP/S or POP3/S mailbox and publish e-mails as news entries/blog posts inside Confluence. 
Special about it is that the space, in which the news should be published can be defined as an e-mail-address wildcard (e-mail+spacekey@domain.net).

## Installation
In your Confluence administration page, upload the JAR file.
After installing you will find a new administration link "Mail2News" under the "General" section. Enter the valid account information for your POP3/IMAP account and check the "Share-With" option if you like to enable sharing pages by e-mail (see below).

## Changes to the original plug-in
### Architecture
I did a lot of refactoring for easier debugging and added a few unit tests.

### Authentication
mail2news tries to resolve the user of the e-mail in the following order:
* first the e-mail address from the To:-header is taken and looked up inside the Confluence internal user directory
* if this e-mail address lookup failed, the username part of the address is taken and used for an username lookup

This plug-in does only work if the sending user can be identified. Anonymous blog posts are _not_ allowed.

### Identifying spaces
In addition to the original mail2news plug-in, this fork is able to identify the space key by
* e-mail address (*wiki+$SPACEKEY@domain.tld*)
* or subject of e-mail (*[$SPACEKEY]* or *$SPACEKEY: some title*)

mail2new falls back to personal space of the sender if the user who sent the mail has no permission in the given space or the space could not be found.
 
### Sharing with others
Confluence 5 introduced the functionality of sharing pages. This plug-in allows you to share a blog post after it is published.
Every user to whom the blog post shall be published must be mentioned in the *Share-With* header of the e-mail. 

This use case might be very specific to our environment.
- I use the sharing option of Android/Chrome to share the blog post via e-mail (To: wiki@our-site.tld). Every user I want to inform about the page I am currently visiting is put inside the CC field.
- Our Exchange server receives the e-mail and executes the Generic Exchange Transport Agent (GETA, https://github.com/NeosIT/generic-exchange-transport-agent, kudos to @prunkstar)
- A specific GETA plug-in moves the additional recipients from the To/CC mail header to a new *Share-With* header. This prevents that duplicate e-mails will be sent to recipients. The approach ensures that the information base is inside Confluence and that further discussions about the shared site will be handled through the central Confluence platform.
- Exchange moves the transformed e-mail to the inbox of the mail2news POP3/IMAP account
- Confluence / mail2news checks the inbox and shares the created blog post based on the *Share-With* header
- Users receive the default "Sharing" e-mail of conflunece which contains the link to the blog post

## Hacking
- func-test.jar and share-pages.jar can not be downloaded with the current pom.xml. I will fix this later. For the meantime, you have to download the JARs manually into your .m2/repository directory.
- For testing purposes I use Apache James 3.0 beta. Create a new domain (*./james-cli.sh adddomain "localhost"*) and create three accounts (*./james-cli.sh adduser "wiki@localhost" wiki && ./james-cli.sh adduser "test@localhost" test && ./james-cli.sh adduser "admin@localhost" admin*). Send an e-mail from user admin@localhost account. Currently I am doing this by hand with thunderbird.
 
## Contact
If you have further questions don't hesitate to write me: confluence-mail2new@schakko.de / http://twitter.com/schakko
