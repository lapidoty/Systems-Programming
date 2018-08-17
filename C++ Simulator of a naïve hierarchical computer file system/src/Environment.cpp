//
// Created by yuval on 09/11/17.
//

#include "../include/Environment.h"
#include "../include/GlobalVariables.h"
#include <iostream>
#include <algorithm>


using namespace std;

Environment::Environment() : commandsHistory(), fs(FileSystem()) , path(""),ParentOfSource(), fullCommand("") , notFoundRoot(false), AlreadyAnounced(false), commandAddedToHistory(false), KnownCommand(false){


}

void Environment::start() {
    std::cout<<fs.getWorkingDirectory().getAbsolutePath() + ">";
    std::string line; //stores the most recent line of input
    string prompt = ">";

    while (std::getline(std::cin, line)) //read entire lines at a time
    {

        AlreadyAnounced = false;
        if((verbose==2) | (verbose==3)) {
            if(line!="exit")
            std::cout << fs.getWorkingDirectory().getAbsolutePath() + ">" + line << endl;
            else
                std::cout << "/>"<<endl;
        }
    KnownCommand=false;
        commandAddedToHistory= false;
        notFoundRoot = false;
        fullCommand = line;

        pwdCommand(line);
        cdCommand(line);
        mkdirCommand(line);
        lsCommand(line);
        mkfileCommand(line);
        cpCommand(line);
        mvCommand(line);
        renameCommand(line);
        rmCommand(line);
        historyCommand(line);
        verboseCommand(line);
        execCommand(line);
        errorCommand(line);

        if (line=="exit") {
            return;
        }
    }

}

FileSystem &Environment::getFileSystem() {
    return fs;
}

string Environment::findNextDirectoryAtString(string input, bool changePathField) {
    string output;
    bool found = false;
    if (input[0] == '/') {
        if (input.length() == 1) {
            return input;
        } else {
            input = input.substr(1);
            fs.setWorkingDirectory(&fs.getRootDirectory());
            if (changePathField)
                path = path.substr(1);


        }

    }

    for (size_t i = 0; (i < input.size()) & (!found); i++) {

        if (input[i] == '/') {
            input = input.substr(0, i);
            output = input;

            if (changePathField)
                path = path.substr(i + 1);

            found = true;
        }

    }
    if (!found) {
        output = input;

    }

    return output;
}

void Environment::lsCommand(string line) {

    if ((line[0] == 'l') & (line[1] == 's')) {
        KnownCommand = true;
        Directory &startWork = fs.getWorkingDirectory();
        BaseFile *SourceToSort = nullptr;
        bool SortBySize = false;


        auto const posOfSortBy = line.find_first_of('-');
        if (posOfSortBy == 3) {
            line.erase(posOfSortBy - 1, posOfSortBy);
            SortBySize = true;
        }

        if ((line == "ls /") | (line == "ls ..")) {
            if(line == "ls /") {
                LsCommand *ls = new LsCommand(fullCommand);
                ls->setSortBySize(SortBySize);
                ls->setSource(&fs.getRootDirectory());
                ls->execute(fs);
                addToHistory(ls);
                commandAddedToHistory = true;
            }
            else{
                LsCommand *ls = new LsCommand(fullCommand);
                ls->setSortBySize(SortBySize);
                ls->setSource(fs.getWorkingDirectory().getParent());
                ls->execute(fs);
                addToHistory(ls);
                commandAddedToHistory = true;
            }
        } else {
            if (line != "ls") {
                if(line[3]=='/'){
                    fs.setWorkingDirectory(&fs.getRootDirectory());
                }
                int posOfDestination = findLastOfWhiteSpace(line);
                auto DestinationPath = line.substr(posOfDestination + 1);


                auto const posOfSource = DestinationPath.find_last_of('/');
                auto Source = DestinationPath.substr(posOfSource + 1);


                size_t nDestinationPath = std::count(DestinationPath.begin(), DestinationPath.end(), '/');
                DestinationPath = DestinationPath.substr(0, posOfSource);

                if (nDestinationPath == 0) {
                    Source = DestinationPath;
                    DestinationPath = "";
                }

                SourceToSort = returnSourceAtPath(Source, DestinationPath, nDestinationPath, true, false, false);
            }
            if (!notFoundRoot) {

                LsCommand *ls = new LsCommand(fullCommand);
                ls->setSortBySize(SortBySize);
                ls->setSource(SourceToSort);
                ls->execute(fs);
                addToHistory(ls);
                commandAddedToHistory = true;
            }
        }
        fs.setWorkingDirectory(&startWork);

    }
}


void Environment::cdCommand(string line) {
    if ((line[0] == 'c') & (line[1] == 'd')) {
        KnownCommand = true;
        Directory &startWork = fs.getWorkingDirectory();
        if((line[3]=='/')&(line!="cd /")){
            fs.setWorkingDirectory(&fs.getRootDirectory());
            line.replace(2,2," ");

        }

        vector<string> namesOfDics;
        int posOfPath = findLastOfWhiteSpace(line);
        const auto DestinationPath = line.substr(posOfPath + 1);
        size_t n = std::count(DestinationPath.begin(), DestinationPath.end(), '/');
        path = DestinationPath;
        for (size_t i = 0; i <= n; i++) {
            string directory = findNextDirectoryAtString(path, true);

            namesOfDics.push_back(directory);

        }


        CdCommand *cd = new CdCommand(fullCommand);
        cd->setVectorOfNamesOfDics(namesOfDics);
        cd->execute(fs);
        addToHistory(cd);
        commandAddedToHistory=true;

        if(cd->getNotFound())
            fs.setWorkingDirectory(&startWork);
    }
}

void Environment::pwdCommand(string line) {
    if (line == "pwd") {
        KnownCommand = true;
        PwdCommand *pwd = new PwdCommand(fullCommand);
        pwd->execute(fs);
        addToHistory(pwd);
        commandAddedToHistory=true;
    }
}

void Environment::mkdirCommand(string line) {
    if ((line[0] == 'm' )& (line[1] == 'k') & (line[2] == 'd') & (line[3] == 'i') & (line[4] == 'r')) {
        KnownCommand = true;

        vector<string> namesOfDics;
        int posOfPath = findLastOfWhiteSpace(line);
        const auto DestinationPath = line.substr(posOfPath + 1);
        size_t n = std::count(DestinationPath.begin(), DestinationPath.end(), '/');
        path = DestinationPath;


        for (size_t i = 0; i <= n; i++) {
            string directory = findNextDirectoryAtString(path, true);
            namesOfDics.push_back(directory);

        }


        MkdirCommand *mkdir = new MkdirCommand(fullCommand);
        mkdir->setVectorOfNamesOfDics(namesOfDics);
        mkdir->IsPath(true);
        mkdir->execute(fs);
        addToHistory(mkdir);
        commandAddedToHistory=true;

    }
}

void Environment::mkfileCommand(string line) {
    if ((line[0] == 'm') & (line[1] == 'k' )& (line[2] == 'f') &( line[3] == 'i') & (line[4] == 'l') & (line[5] == 'e')) {
        KnownCommand = true;
        Directory &startWork = fs.getWorkingDirectory();

        vector<string> namesOfDics;
        int *size = new int;
        int posOfSize = findLastOfWhiteSpace(line);
        const auto sizeString = line.substr(posOfSize + 1);
        line = line.substr(0, posOfSize);
        line = line.substr(7);
        std::string::size_type sz;
        *size = std::stoi(sizeString, &sz);
        size_t n = std::count(line.begin(), line.end(), '/');
        path = line;
        string fileName;
        if (n != 0) {
            auto const posOfSource = path.find_last_of('/');
            auto Source = path.substr(posOfSource + 1);
            path = path.substr(0, posOfSource);
            fileName = Source;
            for (size_t i = 0; i < n; i++) {
                string directory = findNextDirectoryAtString(path, true);
                namesOfDics.push_back(directory);

            }
        }
        else{
            fileName=line;
        }



        MkfileCommand *mkfile = new MkfileCommand(fullCommand);
        mkfile->setVectorOfNamesOfDics(namesOfDics);
        mkfile->setName(fileName);
        mkfile->setSizeOfFile(*size);
        mkfile->execute(fs);
        addToHistory(mkfile);
        commandAddedToHistory=true;




        fs.setWorkingDirectory(&startWork);
        delete size;
        size = nullptr;


    }

}

void Environment::cpCommand(string line) {
    Directory &startWork = fs.getWorkingDirectory();
    BaseFile *SourceToCopy;
    Directory *DestinationToCopyTo;
    if ((line[0] == 'c') & (line[1] == 'p')) {
        KnownCommand = true;
        int posOfDestination = findLastOfWhiteSpace(line);
        const auto DestinationPath = line.substr(posOfDestination + 1);
        line = line.substr(0, posOfDestination);

        auto const posOfSource = line.find_last_of('/');
        auto Source = line.substr(posOfSource+1);
        line = line.substr(0, posOfSource);

        int posOfPathSource = findLastOfWhiteSpace(line);
        auto PathSource = line.substr(posOfPathSource + 1);
        line = line.substr(3, posOfPathSource);

        size_t nSourcePath = std::count(PathSource.begin(), PathSource.end(), '/');
        size_t nDestinationPath = std::count(DestinationPath.begin(), DestinationPath.end(), '/');

        if (findLastOfWhiteSpace(Source)==2) {

            Source = PathSource;
            PathSource = "";
        }

        SourceToCopy = returnSourceAtPath(Source, PathSource, nSourcePath, false, false,true);
        if (SourceToCopy != nullptr)
            DestinationToCopyTo = returnDestinationAtPath(DestinationPath, nDestinationPath,false,true);

        if ((SourceToCopy != nullptr )& (DestinationToCopyTo != nullptr)) {

            CpCommand *cp = new CpCommand(fullCommand);
            cp->setSource(SourceToCopy);
            cp->setDestination(DestinationToCopyTo);
            cp->execute(fs);
            addToHistory(cp);
            commandAddedToHistory=true;
        }

        fs.setWorkingDirectory(&startWork);
    }

}

void Environment::mvCommand(string line) {
    Directory &startWork = fs.getWorkingDirectory();
    BaseFile *SourceToMove;
    Directory *DestinationToMoveTo;
    if ((line[0] == 'm') & (line[1] == 'v')) {
        KnownCommand = true;
        int posOfDestination = findLastOfWhiteSpace(line);
        const auto DestinationPath = line.substr(posOfDestination + 1);
        line = line.substr(0, posOfDestination);

        auto const posOfSource = line.find_last_of('/');
        auto Source = line.substr(posOfSource + 1);
        line = line.substr(0, posOfSource);

        int posOfPathSource = findLastOfWhiteSpace(line);
        auto PathSource = line.substr(posOfPathSource + 1);
        line = line.substr(3, posOfPathSource);

        size_t nSourcePath = std::count(PathSource.begin(), PathSource.end(), '/');
        size_t nDestinationPath = std::count(DestinationPath.begin(), DestinationPath.end(), '/');

        if (findLastOfWhiteSpace(Source) == 2) {

            Source = PathSource;
            PathSource = "";
        }

        if ((Source == "..") |((fs.getWorkingDirectory().getParent()!= nullptr)
                             && (Source==fs.getWorkingDirectory().getParent()->getName()))) {
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("Can't move directory");
            error->execute(fs);
            delete error;
            error = nullptr;

        } else {
            SourceToMove = returnSourceAtPath(Source, PathSource, nSourcePath, false, true, false);
            if (!notFoundRoot) {
                DestinationToMoveTo = returnDestinationAtPath(DestinationPath, nDestinationPath, true, false);


                if ((SourceToMove != nullptr )& (DestinationToMoveTo != nullptr)) {
                    MvCommand *mv = new MvCommand(fullCommand);
                    mv->setSource(SourceToMove);
                    mv->setDestination(DestinationToMoveTo);
                    mv->setParentOfSource(ParentOfSource);
                    mv->execute(fs);
                    addToHistory(mv);
                    commandAddedToHistory = true;
                }
            }
            fs.setWorkingDirectory(&startWork);

        }
    }
}

void Environment::renameCommand(string line) {
    Directory &startWork = fs.getWorkingDirectory();
    BaseFile *SourceToRename;
    bool existsAlready=false;
    if ((line[0] == 'r') & (line[1] == 'e') & (line[2] == 'n') & (line[3] == 'a') & (line[4] == 'm')& (line[5] == 'e')) {
        KnownCommand = true;
        if((line[7]=='/')){
            fs.setWorkingDirectory(&fs.getRootDirectory());
            line.replace(7,1,"");

        }
        int posOfRename = findLastOfWhiteSpace(line);
        const auto NewName = line.substr(posOfRename + 1);
        line = line.substr(0, posOfRename);

        auto const posOfSource = line.find_last_of('/');
        auto Source = line.substr(posOfSource + 1);
        line = line.substr(0, posOfSource);

        int posOfPathSource = findLastOfWhiteSpace(line);
        auto PathSource = line.substr(posOfPathSource + 1);
        line = line.substr(6, posOfPathSource);

        size_t nSourcePath = std::count(PathSource.begin(), PathSource.end(), '/');


        if(Source==fs.getWorkingDirectory().getName()){
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("Can't rename the working directory");
            error->execute(fs);
            delete error;
            error = nullptr;

        }else {

            if (nSourcePath == 0) {
                Source = PathSource;
                PathSource = "";
            }

            SourceToRename = returnSourceAtPath(Source, PathSource, nSourcePath, false, false, false);
            if (ParentOfSource != nullptr) {

                if ((dynamic_cast<Directory *>(ParentOfSource)->SearchFile(NewName)) |
                    (dynamic_cast<Directory *>(ParentOfSource)->SearchDirectory(NewName))) {

                    existsAlready = true;
                }

            }

            if ((SourceToRename != nullptr) & (!existsAlready)) {
                RenameCommand *rename = new RenameCommand(fullCommand);
                rename->setSource(SourceToRename);
                rename->setNewName(NewName);
                rename->execute(fs);
                addToHistory(rename);
                commandAddedToHistory = true;

            }
        }
        fs.setWorkingDirectory(&startWork);
    }
}


void Environment::rmCommand(string line) {
    Directory &startWork = fs.getWorkingDirectory();
    BaseFile *SourceToRemove;

    if ((line[0] == 'r' )& (line[1] == 'm')) {
        KnownCommand = true;
        if ((line.length() == 4) &(line[3] == '/')) {

            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("Can't remove directory");
            error->execute(fs);
            delete error;
            error = nullptr;


        } else {
            auto const posOfSource = line.find_last_of('/');
            auto Source = line.substr(posOfSource + 1);
            line = line.substr(0, posOfSource);

            int posOfPathSource = findLastOfWhiteSpace(line);
            auto PathSource = line.substr(posOfPathSource + 1);
            line = line.substr(3, posOfPathSource);

            size_t nSourcePath = std::count(PathSource.begin(), PathSource.end(), '/');

            if (Source == fs.getWorkingDirectory().getName()) {
                ErrorCommand *error = new ErrorCommand(fullCommand);
                error->setAnnouncment("Can't remove directory");
                error->execute(fs);
                delete error;
                error = nullptr;

            } else {
                if (nSourcePath == 0) {
                    Source = PathSource;
                    PathSource = "";
                }


                SourceToRemove = returnSourceAtPath(Source, PathSource, nSourcePath, false, false, false);
                if (SourceToRemove != nullptr) {

                    RmCommand *remove = new RmCommand(fullCommand);
                    remove->setSource(SourceToRemove);
                    remove->setParentOfSource(ParentOfSource);
                    remove->execute(fs);
                    addToHistory(remove);
                    commandAddedToHistory = true;

                }

                fs.setWorkingDirectory(&startWork);
            }
        }
    }
}

void Environment::historyCommand(string line) {
    if ((line[0] == 'h') & (line[1] == 'i') & (line[2] == 's') &(line[3] == 't') & (line[4] == 'o') & (line[5] == 'r')& (line[6] == 'y')) {
        KnownCommand = true;
        HistoryCommand *history = new HistoryCommand(fullCommand, commandsHistory);
        history->execute(fs);
        addToHistory(history);
        commandAddedToHistory=true;
    }
}

void Environment::verboseCommand(string line) {
    if ((line[0] == 'v') & (line[1] == 'e') & (line[2] == 'r') & (line[3] == 'b') &(line[4] == 'o') & (line[5] == 's') &
            (line[6] == 'e')) {
        KnownCommand = true;
        int *number = new int;
        int posOfSize = findLastOfWhiteSpace(line);
        const auto numberString = line.substr(posOfSize + 1);
        std::string::size_type sz;
        *number = std::stoi(numberString, &sz);

        if((*number!=0) &( *number!=1 )&( *number!=2) & (*number!=3)){
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("Wrong verbose input");
            error->execute(fs);
            delete error;
            error= nullptr;
        }
        else {


            VerboseCommand *verbose = new VerboseCommand(fullCommand);
            verbose->setVerboseNumber(*number);
            verbose->execute(fs);
            addToHistory(verbose);
            commandAddedToHistory=true;

        }
        delete number;
    }
}

void Environment::execCommand(string line) {
    if ((line[0] == 'e') & (line[1] == 'x') & (line[2] == 'e') &(line[3] == 'c')) {
        KnownCommand = true;
        size_t *number = new size_t;
        int posOfSize = findLastOfWhiteSpace(line);
        const auto numberString = line.substr(posOfSize + 1);
        std::string::size_type sz;
        *number = std::stoi(numberString, &sz);

        if ((*number < 0) | (*number > commandsHistory.size()))
        {

            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("Command not found");
            error->execute(fs);
            delete error;
            error= nullptr;
        }
        else {
            ExecCommand *exec = new ExecCommand(fullCommand, commandsHistory);
            exec->setExecNumber(*number);
            exec->execute(fs);
            addToHistory(exec);
            commandAddedToHistory=true;


        }
        delete number;
    }

}

void Environment::errorCommand(string line) {
    if(!commandAddedToHistory){
        int posOfSpace = findFirstOfWhiteSpace(line);
        string UnknowCommand = line.substr(0,posOfSpace);
        ErrorCommand *error = new ErrorCommand(fullCommand);
        error->setAnnouncment(UnknowCommand+": Unknown command");
        if((!KnownCommand)&(line!="exit")) {
            error->execute(fs);
        }
        addToHistory(error);
        commandAddedToHistory=true;
    }

}

BaseFile *Environment::returnSourceAtPath(string Source, string PathSource, size_t n, bool ifLs, bool ifMv,bool ifCp) {
    Directory &startWork = fs.getWorkingDirectory();
    BaseFile *output = nullptr;
    bool notFound = false;
    if (PathSource != "") {
        for (size_t i = 0; (i <= n) & (!notFound); i++) {
            string directory = findNextDirectoryAtString(PathSource, false);

            if (PathSource.length() >= directory.length() + 1)
                PathSource = PathSource.substr(directory.length() + 1);


            CdCommand *cd = new CdCommand(directory);
            vector<string> OneWordVector;
            OneWordVector.push_back(directory);
            cd->setVectorOfNamesOfDics(OneWordVector);
            cd->setLs(ifLs);
            cd->setMv(ifMv);
            cd->setCp(ifCp);

            cd->execute(fs);
            notFound = (*cd).getNotFound();
            delete cd;
            cd = nullptr;
        }
    }
    if (!notFound) {
        if (!fs.getWorkingDirectory().SearchDirectory(Source) &
            !fs.getWorkingDirectory().SearchFile(Source)) {
            if(ifLs) {

                ErrorCommand *error = new ErrorCommand(fullCommand);
                error->setAnnouncment("The system cannot find the path specified");
                error->execute(fs);
                delete error;
                error = nullptr;
                notFoundRoot = true;

            }
            else{
                ErrorCommand *error = new ErrorCommand(fullCommand);
                error->setAnnouncment("No such file or directory");
                error->execute(fs);
                delete error;
                error = nullptr;
                notFoundRoot=true;
            }

        } else {
            output = fs.getWorkingDirectory().ReturnFoundBaseFile(Source);
            ParentOfSource = &fs.getWorkingDirectory();
        }
    } else {
        if(!ifLs) {
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("No such file or directory");
            error->execute(fs);
            delete error;
            error = nullptr;
            notFoundRoot = true;
        }
        else{
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("The system cannot find the path specified");
            error->execute(fs);
            delete error;
            error = nullptr;
            notFoundRoot = true;
        }
    }
    fs.setWorkingDirectory(&startWork);
    return output;
}
Directory *Environment::returnDestinationAtPath(string DestinationPath, size_t n, bool ifMv,bool ifCp) {
    Directory &startWork = fs.getWorkingDirectory();
    Directory *output = nullptr;
    bool notFound = false;
    string directory;
    for (size_t i = 0; (i <= n) & (!notFound); i++) {
        directory = findNextDirectoryAtString(DestinationPath, false);

        if (DestinationPath.length() >= directory.length() + 1)
            DestinationPath = DestinationPath.substr(directory.length() + 1);

        CdCommand *cd = new CdCommand(directory);
        vector<string> OneWordVector;
        OneWordVector.push_back(directory);
        cd->setVectorOfNamesOfDics(OneWordVector);
        cd->setMv(ifMv);
        cd->setCp(ifCp);

        cd->execute(fs);
        notFound = (*cd).getNotFound();
        notFoundRoot=(*cd).getNotFound();
        delete cd;
        cd = nullptr;
    }
    if (!notFound) {
        output = &fs.getWorkingDirectory();
    } else {
        if(!ifCp) {
            ErrorCommand *error = new ErrorCommand(fullCommand);
            error->setAnnouncment("No such file or directory");
            error->execute(fs);
            delete error;
            error = nullptr;
        }
    }
    fs.setWorkingDirectory(&startWork);
    return output;
}

int Environment::findLastOfWhiteSpace(string path) {
    for (int i = path.length() - 1; i > 0; --i) {
        if (path[i] == ' ')
            return i;


    }
    return -1;
}

int Environment::findFirstOfWhiteSpace(string path) {
    for (size_t i = 0 ; i < path.length() - 1; ++i) {
        if (path[i] == ' ')
            return i;


    }
    return -1;
}

void Environment::addToHistory(BaseCommand *command) {

    commandsHistory.push_back(command);

}

const vector<BaseCommand *> &Environment::getHistory() const { return commandsHistory; }

void Environment::clear(){
    std::vector<BaseCommand *>::iterator it;

    it = commandsHistory.begin();
    while (it != commandsHistory.end()) {

        if (*it != nullptr) {
            delete *it;
            *it = nullptr;
            ++it;
        }
    }
}
//START RULE OF FIVE
//------------
Environment::~Environment() {
    clear();
    if((verbose==1) | (verbose==3)){
        std::cout << ("Environment::~Environment()")<<endl;
    }
}

Environment& Environment::operator=(Environment && other) {

    if (this != &other){
        commandsHistory = other.commandsHistory;
        fs = other.fs;
        path=other.path;
        ParentOfSource=other.ParentOfSource;
        fullCommand=other.fullCommand;
        notFoundRoot=other.notFoundRoot;
        AlreadyAnounced=other.AlreadyAnounced;
        commandsHistory=other.commandsHistory;

        std::vector<BaseCommand *>::iterator it;
        vector<BaseCommand*> otherBase= other.getHistory();
        for (it = otherBase.begin(); it != otherBase.end(); ++it) {
            delete *it;
        }
        delete &other.ParentOfSource;

    }

    if((verbose==1) | (verbose==3)){
        std::cout << ("Environment& Environment::operator=(Environment && other)")<<endl;
    }
    return *this;


}

Environment::Environment(Environment &other) : commandsHistory(other.commandsHistory), fs(other.fs) , path(other.path),ParentOfSource(other.ParentOfSource), fullCommand(other.fullCommand) ,
                                               notFoundRoot(other.notFoundRoot), AlreadyAnounced(other.AlreadyAnounced), commandAddedToHistory(other.commandAddedToHistory) , KnownCommand(other.KnownCommand){

    if((verbose==1) | (verbose==3)){
        std::cout << ("Environment::Environment(const Environment &other)")<<endl;
    }
}
Environment::Environment(Environment &&other): commandsHistory(other.commandsHistory), fs(other.fs) , path(other.path),ParentOfSource(other.ParentOfSource), fullCommand(other.fullCommand) ,
                                               notFoundRoot(other.notFoundRoot), AlreadyAnounced(other.AlreadyAnounced), commandAddedToHistory(other.commandAddedToHistory) , KnownCommand(other.KnownCommand){
    std::vector<BaseCommand *>::iterator it;
    vector<BaseCommand*> otherBase= other.getHistory();
    for (it = otherBase.begin(); it != otherBase.end(); ++it) {
        delete *it;
    }
    delete &other.ParentOfSource;

    if((verbose==1) | (verbose==3)){
        std::cout << ("Environment::Environment(Directory &&Environment)")<<endl;
    }
}


Environment& Environment::operator=(const Environment &other) {

    if (this != &other) {

        commandsHistory = other.commandsHistory;
        fs = other.fs;
        path=other.path;
        ParentOfSource=other.ParentOfSource;
        fullCommand=other.fullCommand;
        notFoundRoot=other.notFoundRoot;
        AlreadyAnounced=other.AlreadyAnounced;
        commandsHistory=other.commandsHistory;

    }

    if((verbose==1) | (verbose==3)){
        std::cout << ("Environment& Environment::operator=(const Environment &other)")<<endl;
    }
    return *this;
}

//------------
//END RULE OF FIVE


