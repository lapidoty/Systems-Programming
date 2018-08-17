# C++ Simulator of a naïve hierarchical computer file system
" In this assignment you will write a C++ program that simulates a naïve hierarchical computer file
system.
To communicate with the file system, you will implement a command-line/terminal that enables
the user to create/delete files/folders, print the contents of a directory etc.
The file system consists of a root directory (“/”) and optionally other files and directories
underneath.
The program has a global variable called verbose to control its output. See the verbose
command for details.
Your program will be executed in the following way: “./bin/fs”. Your program must not receive
any external arguments "

# Multi-Threaded Actor Thread Pool implementaion, which used for University Management System
" In the following assignment you are required to implement an Actor ThreadPool, and to use it to build a University
Management System. In Actor thread pool, each actor has a queue of actions. One can submit new actions for
actors and the pool makes sure that each actor will run its actions in the order they were received, while not
blocking other threads. The threads in the pool are assigned dynamically to the actors. As an SPL181 team you
will implement an Actor Thread Pool."

# Multi-Threaded movie rental program using Client/Server based on TCP protocol 
" In this assignment you will implement an online movie rental service (R.I.P. Blockbuster)
server and client. The communication between the server and the client(s) will be
performed using a text based communication protocol, which will support renting, listing
and returning of movies. Please read the entire document before starting.
The implementation of the server will be based on the Thread-Per-Client (TPC) and
Reactor servers taught in class. The servers, as seen in class, do not support bi-directional
message passing. Any time the server receives a message from a client it can reply back
to that specific client itself, but what if we want to send messages between clients, or
broadcast an announcment to all clients? The first part of the assignment will be to
replace some of the current interfaces with new interfaces that will allow such a case.
Note that this part changes the servers pattern and must not know the specific protocol
it is running. The current server pattern also works that way (Generics and interfaces).
Once the server implementation has been extended you will have to implement an
example protocol. We will implement the movie rental service over the User service textbased
protocol. The User Service Text-based protocol is the base protocol which will
define the message structure and base command. Given an implementation of the
protocol we can implement many user service applications. The service you will build is a
movie rental service. Since the service requires data to be saved about each user and
available movies for rental, we will implement a simple JSON text database which will be
read when the server starts and updated each time a change is made.
Note that these kinds of services that use passwords and/or money exchange require
additional encryption protocols to pass sensitive data. In our assignment we will ignore
security and focus on network programming.
You will also implement a simple terminal-like client in C++. To simplify matters,
commands will be written by keyboard and sent “as is” to the server "
