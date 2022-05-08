# PCS_DFS
## CSMC 626:Priniples of Computer Security
## Distributed File System- Phase 1 

[GitHub Link for this project](https://github.com/MadhavGTX47/PCS_DFS).

### Team Members & Contribution:-

- Sai Madhav Kolluri : ND92132 - Worked on the Replica Server Implementation and Environment Setup.
- Sai Jahnavi Bachu : LU59970  - Worked on the Replica Server Implementation
- Mounica Uddagiri : QL23899   - Worked on the Master and the Replica Server Implementation
- Venkata Rama Lakshman Nukala :HR39332 - Worked on the Client and Master Server Implementation

## Necessary Stuff to know 

- We used Eclipse to develop this project in JAVA, and the Library we used explicitly is RMI  apart from the rest are default libraries.
- Make sure your JAVA is installed on your pc (The latest version if possible)
- To Run You need at least one instance of the console running each:- Replica, Client, Master.
- The overall flow to execute would be run the Replica servers, then the master and the client, and all the commands will be sent from a client.

## Libraries Used
- [Package java.rmi](https://docs.oracle.com/javase/8/docs/api/java/rmi/package-summary.html)

##  Step-by-Step instructions:-
- First Install Eclipse in the machines where you will be running the code (i.e Server, Client, Master.)
- Then Open Eclipse and Click File -> Open Projects from File System and import the Project Folder.
- Now once the project is open go to *src/demo*, till here it is common for all the machines.
- Now we should setup each of the following in order.

# Replica Server
- First got to the file **ReplicaServer.java** and change the port number for each replica. 
- Now change the replica details for each replica as shown below in the *line:177*
```sh
Example:-
ReplicaServer rs = new ReplicaServer(0, "./");
```
```sh
ReplicaServer rs = new ReplicaServer(Replicanumber, "Directory path");
```
- Now  Run the **ReplicaServer.java**, then in the console, it should show Replica is Ready.
- And Repeat this Process on Each Replica. And keep in mind every Replica should have a different port number and different Replica Number. 

> As we start each replica a Folder named replica along with a replica number will be created above the current directory path. 

# Master Server
- For the Master Server, run the **MasterServer.java**, then in the Console, it should show It is connected to the Replica Servers.

# Client Server
- For the Client, run the **ClientServer.java**, then in the Console it should show Possible options, And the user has to type the name of the operation he needs.

```sh
read
write 
rename
delete
```
