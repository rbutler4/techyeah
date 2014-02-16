#include <stdio.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <unistd.h>

int main( int argc, char *argv[] )
{
    int sockfd, clisockfd, portno;
    char * start = "hello";
    char * end = "bye";
    socklen_t clilen;
    char buffer[256];
    char contentBuffer[255];
    struct sockaddr_in serv_addr, cli_addr;
    int  n;
    //int optval;

    /* First call to socket() function */
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
    {
        perror("ERROR opening socket");
        return(1);
    }

    /* Initialize socket structure */
    memset((char *) &serv_addr, 0, sizeof(serv_addr));
    portno = 5000;
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    if (bind(sockfd, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0)
    {
        perror("ERROR on binding");
        return(1);
    }

    printf("waiting for connections...\n");
    listen(sockfd,5);
    clilen = (socklen_t) sizeof(cli_addr);

    clisockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
    printf("incoming connection\n");
    if (clisockfd < 0) 
    {
        perror("ERROR on accept");
        return(1);
    }
    printf("connected\n");

    while (strcmp(end, contentBuffer) !=0)
    {
        memset(buffer,0,256);
        memset(contentBuffer,0,255);
        /* If connection is established then start communicating */
        n = read( clisockfd,buffer,255 );
        if (n < 0)
        {
            perror("ERROR reading from socket");
            return(1);
        }

        strncpy(contentBuffer,buffer,strlen(buffer) - 1); 
        if (strcmp(start, contentBuffer) ==0)
        {
            printf("command: %s\n",buffer);
            n = write(clisockfd,"Roger that\n",11);
            if (n < 0)
            {
                perror("ERROR writing to socket");
                return(1);
            }
        }
        else 
        {
            printf("Unknown command: %s\n",buffer);
            n = write(clisockfd,"Cmd\n",4);
            if (n < 0)
            {
                perror("ERROR writing to socket");
                return(1);
            }           
        }
    }
    printf("closing connection\n");
    close(sockfd);
    return 0;
}