//
// Created by yuval on 09/11/17.
//

#include "../include/Commands.h"
#include "../include/GlobalVariables.h"
#include <iostream>

using namespace std;

//BaseCommand
BaseCommand::BaseCommand(string args) : args(args), fullCommand() { fullCommand = "";}
string BaseCommand::getArgs() { return args; }
void BaseCommand::setFullCommand(string newCommand) { fullCommand = newCommand; }
BaseCommand::~BaseCommand() {}


//Pwd Command
PwdCommand::PwdCommand(string args) : BaseCommand(args) {}
void PwdCommand::execute(FileSystem &fs) {
    string output;
    string parent;
    bool root = false;
    string DirectoryParentName;

    Directory &startWork = fs.getWorkingDirectory();
    while (!root) {
        Directory &workDic = fs.getWorkingDirectory();
        string DirectoryName = fs.getWorkingDirectory().getName();

        if (&fs.getWorkingDirectory() != &fs.getRootDirectory())
            DirectoryParentName = fs.getWorkingDirectory().getParent()->getName();

        if ((DirectoryName == "/") | (DirectoryParentName == "/")) {
            output = DirectoryName + output;
        } else {
            output = "/" + DirectoryName + output;
        }


        if (&workDic == &fs.getRootDirectory())
            root = true;
        else
            fs.setWorkingDirectory(workDic.getParent());


    }

    fs.setWorkingDirectory(&startWork);
    std::cout << output << endl;


}
string PwdCommand::toString() {
    return "pwd";
}

//Cd Command
CdCommand::CdCommand(string args) : BaseCommand(args) , OneByOne(false),ls(false), notFound(false)
,Names(), mkdir(false) , mv(false) , cp(false){}
void CdCommand::execute(FileSystem &fs) {

    if (Names.size() == 1)
        oneByOne(fs, Names[0]);

    else
        WholePath(fs);
}
string CdCommand::toString() {return "cd";}
void CdCommand::setMkdirAction(bool arg) { mkdir = arg; }
void CdCommand::setLs(bool arg) { ls = arg; }
bool CdCommand::getNotFound() { return notFound; }
void CdCommand::setVectorOfNamesOfDics(vector<string> NewNames) {
    Names = NewNames;
}
void CdCommand::setMv(bool Newmv) {mv = Newmv;}
void CdCommand::setCp(bool NewCp) {cp=NewCp;}
void CdCommand::oneByOne(FileSystem &fs, string toMove) {

    if ((toMove == "..") | (toMove == "/"))
    {
        if (toMove == "..") {
            if(&fs.getWorkingDirectory() == &fs.getRootDirectory()){
                notFound=true;
            }
            else {
                fs.setWorkingDirectory(fs.getWorkingDirectory().getParent());
            }


        }
        if ((toMove == "/") & (&fs.getWorkingDirectory() != &fs.getRootDirectory()))
            fs.setWorkingDirectory(&fs.getRootDirectory());
    } else {

        if (fs.getWorkingDirectory().SearchDirectory(toMove)) {

            fs.setWorkingDirectory(dynamic_cast<Directory *> (fs.getWorkingDirectory().ReturnFoundBaseFile(
                    toMove)));
        }
        else {
            if (!mkdir & !ls & !mv) {
                {
                    ErrorCommand *error = new ErrorCommand(BaseCommand::getArgs());

                    if (cp)
                        error->setAnnouncment("No such file or directory");
                    else
                        error->setAnnouncment("The system cannot find the path specified");

                    error->execute(fs);
                    delete error;
                    error = nullptr;
                    notFound = true;
                }
            }
            if(!ls)
                notFound = true;
        }
    }
}
void CdCommand::WholePath(FileSystem &fs) {
    for (size_t i = 0; i < Names.size(); ++i) {
        oneByOne(fs, Names[i]);

    }
}
void CdCommand::setOneByOne(bool arg) { OneByOne = arg; }

//MkdirCommand
MkdirCommand::MkdirCommand(string args) : BaseCommand(args) , isPath(false) , Names(){}
void MkdirCommand::execute(FileSystem &fs) {
    Directory &startWork = fs.getWorkingDirectory();
    bool alreadyExists = false;
    for (size_t i = 0; i < Names.size(); ++i) {
        if (fs.getWorkingDirectory().SearchDirectory(Names[i])) {
            if (((i != Names.size() - 2) | (((i == Names.size() - 2) & (Names.size()==2)))) & (Names.size()!=1)){

                CdCommand *cd = new CdCommand(Names[i]);
                vector<string> OneWordVector;
                OneWordVector.push_back(Names[i]);
                cd->setVectorOfNamesOfDics(OneWordVector);
                (*cd).setMkdirAction(true);
                cd->execute(fs);
                delete cd;
                cd = nullptr;
            } else {

                ErrorCommand *error = new ErrorCommand(BaseCommand::getArgs());
                error->setAnnouncment("The directory already exists");
                error->execute(fs);
                delete error;
                error= nullptr;
                alreadyExists=true;

            }
        } else {
            if (!fs.getWorkingDirectory().SearchDirectory(Names[i])&!alreadyExists) {
                Directory *toAdd = new Directory(Names[i], &fs.getWorkingDirectory());

                fs.getWorkingDirectory().addFile(toAdd);
                CdCommand *cd = new CdCommand(Names[i]);
                vector<string> OneWordVector;
                OneWordVector.push_back(Names[i]);
                cd->setVectorOfNamesOfDics(OneWordVector);
                cd->setOneByOne(true);
                (*cd).setMkdirAction(true);
                cd->execute(fs);
                delete cd;
                cd = nullptr;

            }
        }

    }
    fs.setWorkingDirectory(&startWork);

}
void MkdirCommand::IsPath(bool arg) { isPath = arg; }
string MkdirCommand::toString() {return "mkdir";}
void MkdirCommand::setVectorOfNamesOfDics(vector<string> NewNames) {
    std::vector<string>::iterator it;

    for (it = NewNames.begin(); it != NewNames.end();++it) {
        Names.push_back(*it);

    }

}
MkdirCommand::~MkdirCommand() {

    std::vector<string>::iterator it;
    for (it = Names.begin(); it != Names.end();) {
        Names.erase(it);
    }
}

//LsCommand
LsCommand::LsCommand(string args) : BaseCommand(args), sortBySize(),SourceToSort() {}
void LsCommand::execute(FileSystem &fs) {
    Directory *WorkDic;
    if (SourceToSort != nullptr)
        WorkDic = dynamic_cast<Directory *>(SourceToSort);

    else
        WorkDic = &fs.getWorkingDirectory();

    if (!sortBySize)
        WorkDic->sortByName();

    else
        WorkDic->sortBySize();


    vector<BaseFile *> subDicList = WorkDic->getChildren();
    std::vector<BaseFile *>::iterator it;

    for (it = subDicList.begin(); it != subDicList.end(); ++it) {
        if (*it != nullptr) {

            string name = (*it)->getName();
            int size = (*it)->getSize();
            string type = (*it)->returnType();

            if (type == "Directory") {
                type = "DIR";
            } else {
                type = "FILE";
            }

            std::cout << type + "\t" + name + "\t";
            std::cout << size << endl;

        }
    }

}
string LsCommand::toString() {return "ls";}
void LsCommand::setSortBySize(bool arg) { sortBySize = arg; }
void LsCommand::setSource(BaseFile *NewSourceToSort) { SourceToSort = NewSourceToSort; }
LsCommand::~LsCommand() {return;}
LsCommand::LsCommand(LsCommand &other) : BaseCommand::BaseCommand(other.getArgs()) , sortBySize(),SourceToSort() {
    sortBySize = other.sortBySize;
    if (SourceToSort->returnType() == "Directory")
        (SourceToSort) = new Directory(*(dynamic_cast<Directory *>(SourceToSort)));

    else
        SourceToSort = other.SourceToSort;
}
LsCommand::LsCommand(LsCommand && other) : BaseCommand::BaseCommand(other.getArgs()), sortBySize(), SourceToSort()
{
    sortBySize = other.sortBySize;
    SourceToSort = other.SourceToSort;
    other.SourceToSort = nullptr;
}


// MkfileCommand
MkfileCommand::MkfileCommand(string args) : BaseCommand(args), sizeOfFile(),Names(),nameToMake() {}
void MkfileCommand::execute(FileSystem &fs) {
    Directory &startWork = fs.getWorkingDirectory();

    bool notFoundPath = false;

    for (size_t i = 0; (i < Names.size()) & (!notFoundPath); ++i) {
        CdCommand *cd = new CdCommand(Names[i]);
        vector<string> OneWordVector;
        OneWordVector.push_back(Names[i]);
        cd->setVectorOfNamesOfDics(OneWordVector);
        cd->execute(fs);
        notFoundPath=cd->getNotFound();
        delete cd;
        cd = nullptr;
    }

    if (fs.getWorkingDirectory().SearchFile(nameToMake)){
        ErrorCommand *error = new ErrorCommand(BaseCommand::getArgs());
        error->setAnnouncment("File already exists");
        error->execute(fs);
        delete error;
        error= nullptr;

    } else {
        if(!notFoundPath) {
            File *toAdd = new File(nameToMake, sizeOfFile);
            fs.getWorkingDirectory().addFile(toAdd);
        }


    }


    fs.setWorkingDirectory(&startWork);
}
string MkfileCommand::toString() {return "mkfile";}
void MkfileCommand::setSizeOfFile(int arg) { sizeOfFile = arg; }
void MkfileCommand::setVectorOfNamesOfDics(vector<string> NewNames) {
    std::vector<string>::iterator it;

    for (it = NewNames.begin(); it != NewNames.end();++it) {
        Names.push_back(*it);


    }

}
MkfileCommand::~MkfileCommand() {
    std::vector<string>::iterator it;
    for (it = Names.begin(); it != Names.end();) {
        Names.erase(it);
    }
}
void MkfileCommand::setName(string newName) { nameToMake = newName; }

//CpCommand
CpCommand::CpCommand(string args) : BaseCommand(args), SourceToCopy(), DestinationToCopyTo() {}
void CpCommand::execute(FileSystem &fs) {
    if(!(DestinationToCopyTo)->SearchFile(SourceToCopy->getName())) {
        if ((SourceToCopy)->returnType() == "Directory") {
            Directory *toAddDIR = new Directory(*(dynamic_cast<Directory*>(SourceToCopy)));
            (DestinationToCopyTo)->addFile(toAddDIR);
        } else
            (SourceToCopy)->copyMe(DestinationToCopyTo);
    }

}
string CpCommand::toString() {return "cp";}
void CpCommand::setDestination(Directory *NewDestinationToCopyTo) {
    DestinationToCopyTo = NewDestinationToCopyTo;
}
void CpCommand::setSource(BaseFile *NewSourceToCopy) {
    SourceToCopy = NewSourceToCopy;
}
CpCommand::~CpCommand() {return;}
CpCommand::CpCommand(CpCommand &other) : BaseCommand::BaseCommand(other.getArgs()), SourceToCopy(), DestinationToCopyTo(){
    if (SourceToCopy->returnType() == "Directory")
        (SourceToCopy) = new Directory(*(dynamic_cast<Directory *>(SourceToCopy)));

    else
        SourceToCopy = other.SourceToCopy;

    if (DestinationToCopyTo->returnType() == "Directory")
        (DestinationToCopyTo) = new Directory(*(dynamic_cast<Directory *>(DestinationToCopyTo)));

    else
        DestinationToCopyTo = other.DestinationToCopyTo;

    return;}
CpCommand::CpCommand(CpCommand &&other): BaseCommand::BaseCommand(other.getArgs()), SourceToCopy(), DestinationToCopyTo() {
    SourceToCopy = other.SourceToCopy;
    DestinationToCopyTo=other.DestinationToCopyTo;
    other.DestinationToCopyTo= nullptr;
    other.SourceToCopy= nullptr;
    return;}


//MvCommand
MvCommand::MvCommand(string args) : BaseCommand(args) , SourceToMove() , DestinationToMoveTo() , ParentOfSourceToMove() {}
void MvCommand::execute(FileSystem &fs) {
    SourceToMove->moveMe(ParentOfSourceToMove, DestinationToMoveTo);
}
string MvCommand::toString() {return "mv";}
void MvCommand::setSource(BaseFile *NewSourceToMove) { SourceToMove = NewSourceToMove; }
void MvCommand::setDestination(Directory *NewDestinationToMoveTo) { DestinationToMoveTo = NewDestinationToMoveTo; }
void MvCommand::setParentOfSource(BaseFile *NewParentOfSourceToMove) { ParentOfSourceToMove = NewParentOfSourceToMove; }
MvCommand::~MvCommand() {return;}
MvCommand::MvCommand(MvCommand &other): BaseCommand::BaseCommand(other.getArgs()) , SourceToMove() , DestinationToMoveTo() , ParentOfSourceToMove(){


    if (SourceToMove->returnType() == "Directory")
       (SourceToMove) = new Directory(*(dynamic_cast<Directory *>(SourceToMove)));

    else
        SourceToMove = other.SourceToMove;


    if (DestinationToMoveTo->returnType() == "Directory")
       (DestinationToMoveTo) = new Directory(*(dynamic_cast<Directory *>(DestinationToMoveTo)));

    else
        DestinationToMoveTo = other.DestinationToMoveTo;


    if (ParentOfSourceToMove->returnType() == "Directory")
        (ParentOfSourceToMove) = new Directory(*(dynamic_cast<Directory *>(ParentOfSourceToMove)));

    else
        ParentOfSourceToMove = other.ParentOfSourceToMove;

}
MvCommand::MvCommand(MvCommand &&other) : BaseCommand::BaseCommand(other.getArgs()), SourceToMove() , DestinationToMoveTo() , ParentOfSourceToMove(){
    SourceToMove=other.SourceToMove;
    DestinationToMoveTo=other.DestinationToMoveTo;
    ParentOfSourceToMove=other.ParentOfSourceToMove;
    other.ParentOfSourceToMove= nullptr;
    other.DestinationToMoveTo= nullptr;
    other.SourceToMove= nullptr;

    return;}


//renameCommand
RenameCommand::RenameCommand(string args) : BaseCommand(args), NewName(), SourceToRename() {}
void RenameCommand::execute(FileSystem &fs) {
    SourceToRename->setName(NewName);
}
string RenameCommand::toString() {return "rename";}
void RenameCommand::setSource(BaseFile *NewSourceToRename) { SourceToRename = NewSourceToRename; }
void RenameCommand::setNewName(string newName) { NewName = newName; }
RenameCommand::~RenameCommand() {return;}
RenameCommand::RenameCommand(RenameCommand &other): BaseCommand::BaseCommand(other.getArgs()) ,NewName(), SourceToRename() {
    NewName = other.NewName;
    if (SourceToRename->returnType() == "Directory")
        (SourceToRename) = new Directory(*(dynamic_cast<Directory *>(SourceToRename)));

    else
        SourceToRename = other.SourceToRename;

    return;}
RenameCommand::RenameCommand(RenameCommand &&other): BaseCommand::BaseCommand(other.getArgs()), NewName(), SourceToRename() {
    NewName = other.NewName;
    SourceToRename = other.SourceToRename;
    other.NewName = nullptr;
    other.SourceToRename= nullptr;
    return;}

//RmCommand
RmCommand::RmCommand(string args) : BaseCommand(args), SourceToRemove() , ParentOfSourceToRemove() {}
void RmCommand::execute(FileSystem &fs) {
    dynamic_cast<Directory* >(ParentOfSourceToRemove)->removeMe(SourceToRemove);


}
string RmCommand::toString() {return "rm";}
void RmCommand::setParentOfSource(
        BaseFile *NewParentOfSourceToRemove) { ParentOfSourceToRemove = NewParentOfSourceToRemove; }
void RmCommand::setSource(BaseFile *NewSourceToRemove) { SourceToRemove = NewSourceToRemove; }
RmCommand::~RmCommand() {return;}
RmCommand::RmCommand(RmCommand &other): BaseCommand::BaseCommand(other.getArgs()) , SourceToRemove() , ParentOfSourceToRemove(){
    if (SourceToRemove->returnType() == "Directory")
        (SourceToRemove) = new Directory(*(dynamic_cast<Directory *>(SourceToRemove)));

    else
        SourceToRemove = other.SourceToRemove;

    if (ParentOfSourceToRemove->returnType() == "Directory")
        (ParentOfSourceToRemove) = new Directory(*(dynamic_cast<Directory *>(ParentOfSourceToRemove)));

    else
        ParentOfSourceToRemove = other.ParentOfSourceToRemove;
}
RmCommand::RmCommand(RmCommand &&other) : BaseCommand::BaseCommand(other.getArgs()), SourceToRemove() , ParentOfSourceToRemove(){
    SourceToRemove = other.SourceToRemove;
    ParentOfSourceToRemove=other.ParentOfSourceToRemove;
    other.SourceToRemove = nullptr;
    other.ParentOfSourceToRemove= nullptr;
}

//HistoryCommand
HistoryCommand::HistoryCommand(string args, const vector<BaseCommand *> &history) : BaseCommand(args),history(history) {}
void HistoryCommand::execute(FileSystem &fs) {

    for (size_t i=0; i< history.size(); ++i) {
        std::cout<<i; std::cout<<"\t" + history[i]->getArgs()<<endl;
    }
}
string HistoryCommand::toString() {return "history";}

//VerboseCommand
VerboseCommand::VerboseCommand(string args) : BaseCommand(args), verboseNumber() {verboseNumber=0;}
void VerboseCommand::execute(FileSystem &fs) {
    verbose = verboseNumber;
}
string VerboseCommand::toString() {return "verbose";}
void VerboseCommand::setVerboseNumber(int NewNumber) {verboseNumber=NewNumber;}

//ExecCommand
ExecCommand::ExecCommand(string args, const vector<BaseCommand *> &history): BaseCommand(args),history(history), execNumber() {}
void ExecCommand::execute(FileSystem &fs) {
    history[execNumber]->execute(fs);
}
string ExecCommand::toString() {return "exec";}
void ExecCommand::setExecNumber(int NewNumber) {execNumber=NewNumber;}

//ErrorCommand
ErrorCommand::ErrorCommand(string args) : BaseCommand(args), Announcment(){}
void ErrorCommand::execute(FileSystem &fs) {
    std::cout<<Announcment<<endl;
}
string ErrorCommand::toString() {return "error";}
void ErrorCommand::setAnnouncment(string NewAnnouncment) {
    Announcment = NewAnnouncment;
}


