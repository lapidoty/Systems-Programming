#ifndef COMMANDS_H_
#define COMMANDS_H_

#include <string>
#include "FileSystem.h"



class BaseCommand {
private:
    string args;
    string fullCommand;

public:
    BaseCommand(string args);
    string getArgs();
    virtual void execute(FileSystem & fs) = 0;
    virtual string toString() = 0;
    void setFullCommand(string fullCommand);
    virtual ~BaseCommand();
};

class PwdCommand : public BaseCommand {
private:
public:
    PwdCommand(string args);
    void execute(FileSystem & fs); // Every derived class should implement this function according to the document (pdf)
    virtual string toString();
};

class CdCommand : public BaseCommand {
private: bool OneByOne; bool ls; bool notFound; vector<string> Names;bool mkdir; bool mv; bool cp;
public:
    CdCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setMkdirAction(bool arg);
    void setLs(bool arg);
    bool getNotFound();
    void setVectorOfNamesOfDics(vector<string> Names);
    void oneByOne(FileSystem &fs,string toMove);
    void WholePath(FileSystem &fs);
    void setOneByOne(bool arg);
    void setMv(bool mv);
    void setCp(bool cp);
};

class LsCommand : public BaseCommand {
private: bool sortBySize; BaseFile *SourceToSort;
public:
    LsCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setSortBySize(bool arg);
    void setSource(BaseFile* SourceToSort);
    // Destructor
    virtual ~LsCommand();

    // Copy Constructor
    LsCommand(LsCommand &other);

    // Move Constructor
    LsCommand(LsCommand &&other);

    // Copy Assignment
    LsCommand &operator=(const LsCommand &other);

    // Move Assignment
    LsCommand &operator=(LsCommand &&other);
};

class MkdirCommand : public BaseCommand {
private:
    bool isPath;
    vector<string> Names;
public:
    MkdirCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void IsPath(bool arg);
    void setVectorOfNamesOfDics(vector<string> Names);
    virtual ~MkdirCommand();
};

class MkfileCommand : public BaseCommand {
private: int sizeOfFile; vector<string> Names; string nameToMake;
public:
    MkfileCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setSizeOfFile(int arg);
    void setVectorOfNamesOfDics(vector<string> Names);
    void setName(string newName);
    virtual ~MkfileCommand();
};

class CpCommand : public BaseCommand {
private:
    BaseFile *SourceToCopy;
    Directory *DestinationToCopyTo;
public:
    CpCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setSource(BaseFile* SourceToCopy);
    void setDestination(Directory *DestinationToCopyTo);
    // Destructor
    virtual ~CpCommand();

    // Copy Constructor
    CpCommand(CpCommand &other);

    // Move Constructor
    CpCommand(CpCommand &&other);

    // Copy Assignment
    CpCommand &operator=(const CpCommand &other);

    // Move Assignment
    CpCommand &operator=(CpCommand &&other);
};

class MvCommand : public BaseCommand {
private:
    BaseFile *SourceToMove;
    Directory *DestinationToMoveTo;
    BaseFile *ParentOfSourceToMove;

public:
    MvCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setSource(BaseFile* SourceToMove);
    void setDestination(Directory *DestinationToMoveTo);
    void setParentOfSource(BaseFile* ParentOfSourceToMove);
    // Destructor
    virtual ~MvCommand();

    // Copy Constructor
    MvCommand(MvCommand &other);

    // Move Constructor
    MvCommand(MvCommand &&other);

    // Copy Assignment
    MvCommand &operator=(const MvCommand &other);

    // Move Assignment
    MvCommand &operator=(MvCommand &&other);
};

class RenameCommand : public BaseCommand {
private:
    string NewName;
    BaseFile *SourceToRename;
public:
    RenameCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setNewName(string newName);
    void setSource(BaseFile* SourceToRename);
    // Destructor
    virtual ~RenameCommand();

    // Copy Constructor
    RenameCommand(RenameCommand &other);

    // Move Constructor
    RenameCommand(RenameCommand &&other);

    // Copy Assignment
    RenameCommand &operator=(const RenameCommand &other);

    // Move Assignment
    RenameCommand &operator=(RenameCommand &&other);
};

class RmCommand : public BaseCommand {
private:
    BaseFile *SourceToRemove;
    BaseFile *ParentOfSourceToRemove;
public:
    RmCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setSource(BaseFile* SourceToRemove);
    void setParentOfSource(BaseFile* ParentOfSourceToRemove);
    // Destructor
    virtual ~RmCommand();

    // Copy Constructor
    RmCommand(RmCommand &other);

    // Move Constructor
    RmCommand(RmCommand &&other);

    // Copy Assignment
    RmCommand &operator=(const RmCommand &other);

    // Move Assignment
    RmCommand &operator=(RmCommand &&other);
};

class HistoryCommand : public BaseCommand {
private:
    const vector<BaseCommand *> & history;
public:
    HistoryCommand(string args, const vector<BaseCommand *> & history);
    void execute(FileSystem & fs);
    string toString();

};


class VerboseCommand : public BaseCommand {
private: int verboseNumber;
public:
    VerboseCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setVerboseNumber(int number);
};

class ErrorCommand : public BaseCommand {
private: string Announcment;
public:
    ErrorCommand(string args);
    void execute(FileSystem & fs);
    string toString();
    void setAnnouncment(string Announcment);

};

class ExecCommand : public BaseCommand {
private:
    const vector<BaseCommand *> & history; int execNumber;
public:
    ExecCommand(string args, const vector<BaseCommand *> & history);
    void execute(FileSystem & fs);
    string toString();
    void setExecNumber(int number);
};


#endif
