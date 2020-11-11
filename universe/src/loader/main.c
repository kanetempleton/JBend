#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main(int argc, char* argv[]) {
    //fprintf(stdout, "four");
    char* s;
    if (argc>1) {
        s = argv[1];

        char op[50];
        char buf[50];

        int x = strtol(s,NULL,10);
        int y = x+5;
        sprintf(op,"%d",y);


        strcpy(buf,"script/run/runjava.sh ");
        strcat(buf,op);
        system(buf);
    }
    else {
        system("script/run/runjava.sh");
    }
    return 1;
}
