#ifndef ENVIRONMENT_H_
#define ENVIRONMENT_H_

#include "Files.h"
#include "Commands.h"

#include <string>
#include <vector>

using namespace std;

class Environment {
private:
    vector<BaseCommand*> commandsHistory;
    FileSystem fs;
    string path;
    BaseFile *ParentOfSource;
    string fullCommand;
    bool notFoundRoot;
    bool AlreadyAnounced;
    bool commandAddedToHistory;
    bool KnownCommand;


public:
    Environment();
    void start();
    FileSystem& getFileSystem() ; // Get a reference to the file system
    void addToHistory(BaseCommand *command); // Add a new command to the history
    const vector<BaseCommand*>& getHistory() const; // Return a reference to the history of commands
    string findNextDirectoryAtString(string path , bool changePathField);
    void lsCommand(string line);
    void pwdCommand(string line);
    void cdCommand(string line);
    void mkdirCommand(string line);
    void mkfileCommand(string line);
    void cpCommand(string line);
    void mvCommand(string line);
    void renameCommand(string line);
    void rmCommand(string line);
    void historyCommand(string line);
    void verboseCommand(string line);
    void execCommand(string line);
    void errorCommand(string line);
    BaseFile * returnSourceAtPath(string Source,string PathSource, size_t n,bool ifLs, bool isMv, bool isCp);
    Directory* returnDestinationAtPath(string DestinationPath, size_t n, bool ifMv, bool isCp);
    int findLastOfWhiteSpace(string path);
    int findFirstOfWhiteSpace(string path);
    void clear();
    // Destructor
    virtual ~Environment();

    // Copy Constructor
    Environment(Environment &other);

    // Move Constructor
    Environment(Environment &&other);

    // Copy Assignment
    Environment &operator=(const Environment &other);

    // Move Assignment
    Environment &operator=(Environment &&other);
};

#endif