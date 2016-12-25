#### Java task to compare different server architectures.

Compared architectures:
1. TCP, running in single thread
2. TCP, new thread for connection
3. TCP, cached thread pool for connection
4. TCP, non-blocking processing
5. TCP, async processing
6. UDP, new thread for request
7. UDP, fixed thread pool for request

Running app: **mvn exec:java@gui** 

Running remote server: **mvn exec:java@server**