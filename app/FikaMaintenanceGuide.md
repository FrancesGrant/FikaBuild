# Fika Maintenance Guide
## Overview
***
This maintenance guide is designed to outline instructions and best practices for effectively
maintaining the Fika software application.  This guide aims to ensure ongoing stability 
and performance by addressing bugs, optimising performance and ensuring necessary security updates
are carried out to keep user data secure.

This guide is primarily aimed at developers and testers that need to understand maintenance processes
for future work. 

## Software Overview 
***
UPDATE WITH FINAL FUNCTIONALITY 
Fika is mobile application that promotes in-person connection between users by providing a service 
that finds equidistant meeting places using two users locations.  The current version in use is Version
1.0 which introduces the core functionalities of the app.  Version 1.0 focuses on establishing the 
foundation of a user base and implements features that allow  users to create profiles, search and 
connect with other users, and create meeting requests. 

The software architecture follows a client-server model.  The mobile app serving as the client and 
a cloud-based server handling the backend operations. The app is developed using Kotlin.  The application
integrates with Google Maps API which provides location-based services.  Firebase Authentication and
Firestore is used to manage user authentication and secure data storage.

## Maintenance Process
***
### Version Control
Git is being used to manage code changes and keep track of the modification history.  Commit messages 
are being used to clearly communicate the changes.

As Git and Github are established in the project it is recommended to use Github Issues for issue
tracking.  Github Issues allow for issue tickets to be created and allocated to members working on
maintenance. 

Define the step-by-step process for handling software maintenance activities. 
This should include procedures for reporting and tracking issues, prioritizing tasks, and resolving 
problems. Provide guidance on how to use issue tracking systems, version control, and collaboration 
tools effectively.

### Bug Fixing
Bugs are to be reported using the issue tracking system outlined in Version Control. When reporting 
a bug it is recommended to provide a clear and descriptive title that summarises the issue encountered.
Provide a detailed description of the bug that highlights the steps to take to reproduce it, include 
relevant data or inputs required to reproduce the bug.  If possible, it is good practice to include
screenshots or supporting files to further explain the bug. The severity of the bug should also be 
recorded (critical, high, medium, low).

### Security Updates
Security patches should be kept up to date so that the frameworks, libraries and dependencies used 
are updated regularly to ensure the app is not vulnerable to security breaches.  

## Release Management
For future releases a dedicated branch in the version control system should be created.  Code freeze
periods should be used during the release of new features so that the features in the release can
be robustly tested and stabilised.  

Version numbering schemes should follow semantic versioning / Major.Minor.Patch, e.g. 1.0.0



