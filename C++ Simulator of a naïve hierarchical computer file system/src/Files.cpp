//
// Created by yuval on 08/11/17.
//

#include "../include/Files.h"
#include "../include/GlobalVariables.h"
#include <algorithm>
#include <cstring>
#include <iostream>

// BaseFile class
BaseFile::BaseFile(std::string name) : name("NoName"){}
string BaseFile::getName() const { return name; }
void BaseFile::setName(std::string newName) {
    name = newName;
}
string BaseFile::returnType() { return "BaseFile"; }
void BaseFile::clear() {}
BaseFile::~BaseFile() {}
void BaseFile::copyMe(BaseFile *parent) {}
void BaseFile::moveMe(BaseFile *MyParent, BaseFile *OtherParent) {}
void BaseFile::removeMe(BaseFile *ToRemove) {}


//File class
File::File(std::string name, int size) : BaseFile(name), size(size) { size = 0; (*this).setName(name); }
int File::getSize() { return size; }
string File::returnType() { return "File"; }
void File::clear() {

}
void File::copyMe(BaseFile *OtherParent) {

    File* toAdd= new File(this->getName(),this->getSize());

    dynamic_cast<Directory*> (OtherParent)->addFile(toAdd);
}
void File::moveMe(BaseFile *MyParent, BaseFile *OtherParent) {
    dynamic_cast<Directory*>(MyParent)->removeFile(this);
    dynamic_cast<Directory*>(OtherParent)->addFile(this);
}
void File::removeMe(BaseFile *ParentOfToRemove) {

    dynamic_cast<Directory*> (ParentOfToRemove)->removeFile(this);

}


//Directory class
void Directory::clear() {

    std::vector<BaseFile *>::iterator it;

    for (it = children.begin(); it != children.end(); ++it) {

        delete *it;

    }

}
// constructor
Directory::Directory(std::string name, Directory *parent) : BaseFile(name), children(),parent(parent) {


    (*this).setName(name);


}

//START RULE OF FIVE
//------------
//copy constructor
Directory::Directory(Directory &other) : BaseFile(other.getName()) , children(),parent(other.parent){
    std::vector<BaseFile *>::iterator it;
    setName(other.getName());
    vector<BaseFile*> otherChi= other.getChildren();
    for (it = otherChi.begin(); it != otherChi.end(); ++it) {
        if((*it)->returnType()=="Directory") {
            Directory *toAdd = new Directory(*(dynamic_cast<Directory*>(*it)));
            addFile(toAdd);
        }
        else
            (*it)->copyMe(this);

    }

    if(verbose==1)
        std::cout << ("Directory::Directory(const Directory &other)")<<endl;
    else if(verbose==3){
        std::cout << ("Directory::Directory(const Directory &other)")<<endl;
    }
}

//move constructor
Directory::Directory(Directory &&other) : BaseFile(other.getName()) , children(),parent(other.parent) {
    children = other.children;
    parent = other.parent;
    other.parent = nullptr;

    if (verbose == 1)
        std::cout << ("Directory::Directory(Directory &&other)") << endl;
    else if (verbose == 3) {
        std::cout << ("Directory::Directory(Directory &&other)") << endl;
    }
}
//copy assignment
Directory &Directory::operator=(const Directory &other) {

    if (this != &other) {
        clear();
        BaseFile::setName(other.getName());
        children = other.children;
        parent = other.getParent();

    }

    if(verbose==1)
        std::cout << ("Directory &Directory::operator=(const Directory &other)")<<endl;
    else if(verbose==3) {
        std::cout << ("Directory &Directory::operator=(const Directory &other)") << endl;
    }
    return *this;


}
//move assignment
Directory &Directory::operator=(Directory &&other) {

    if (this != &other) {

        children = other.children;
        parent = other.parent;
        other.parent = nullptr;
    }

    if(verbose==1)
        std::cout << ("Directory &Directory::operator=(Directory &&other)")<<endl;
    else if(verbose==3) {
        std::cout << ("Directory &Directory::operator=(Directory &&other)")<<endl;
    }
    return *this;
}
//distructor
Directory::~Directory() {


    clear();
    if(verbose==1)
        std::cout << ("Directory::~Directory()")<<endl;
    else if(verbose==3) {
        std::cout << ("Directory::~Directory()")<<endl;
    }

}
//------------
//END RULE OF FIVE


Directory *Directory::getParent() const {

    return parent;
}
void Directory::setParent(Directory *newParent) { parent = newParent; }
void Directory::addFile(BaseFile *file) {

    children.push_back(file);


}
void Directory::removeFile(std::string name) {

    bool found = false;
    std::vector<BaseFile *>::iterator it;
    for (it = children.begin(); (it != children.end()) & !found; ++it) {
        if (*it != nullptr) {
            if ((*it)->getName() == name) {
                if ((*it)->returnType() == "File") {
                    children.erase(it);
                    delete (*it);
                    (*it) = nullptr;
                    found = true;
                } else if ((*it)->returnType() == "Directory") {
                    dynamic_cast<Directory *> (*it)->removeFile(name);
                    children.erase(it);
                    delete (*it);
                    (*it) = nullptr;
                    found = true;
                }


            }
        }
    }

}
void Directory::removeFile(BaseFile *file) {

    std::vector<BaseFile *>::iterator it;
    it=children.begin();
    while(it !=children.end()) {

        if (*it != nullptr) {
            if ((*it) == file)
                it = children.erase(it);

            else
                ++it;
        }
    }


}
bool compareFunctionInt(BaseFile *a, BaseFile *b) { return a->getSize() < b->getSize(); }
bool compareFunctionString(BaseFile *a, BaseFile *b) { return a->getName() < b->getName(); }
void Directory::sortByName() {
    std::sort((children.begin()), (children.end()), compareFunctionString);
}
void Directory::sortBySize() {
    std::sort((children.begin()), (children.end()), compareFunctionInt);
}
std::vector<BaseFile *> Directory::getChildren() { return children; }
int Directory::getSize() {
    int size = 0;
    std::vector<BaseFile *>::iterator it;
    for (it = children.begin(); it != children.end(); ++it) {
        if(*it!= nullptr)
            size = size + (*it)->getSize();

    }
    return size;
}
std::string Directory::getAbsolutePath() {
    string output;
    string dic;
    if (parent != nullptr) {
        dic = (*parent).getAbsolutePath();
        if(dic!="/")
            output = dic + "/" + getName();
        else
            output = dic + getName();

    }
    else
    {
        output = getName();
    }
    return output;
}
string Directory::returnType() { return "Directory"; }
bool Directory::SearchDirectory(string arg) {
    bool found = false;

    std::vector<BaseFile *>::iterator it;

    for (it = children.begin();(it != children.end()) & !found; ++it) {

        if (*it != nullptr) {
            if ((*it)->getName() == arg) {

                    found = true;

            }
        }
    }
    return found;
}
bool Directory::SearchFile(string arg) {
    bool found = false;

    std::vector<BaseFile *>::iterator it;
    for (it = children.begin();(it != children.end()) & !found; ++it) {
        if(*it!= nullptr) {
            if ((*it)->getName() == arg) {

                if ((*it)->returnType() == "File") {
                    found = true;
                }
            }
        }
    }
    return found;
}
BaseFile *Directory::ReturnFoundBaseFile(string arg) {
    bool found = false;
    BaseFile *output;
    std::vector<BaseFile *>::iterator it;
    for (it = children.begin(); (it != children.end()) & !found; ++it) {
        if (*it != nullptr) {
            if ((*it)->getName() == arg) {
                found = true;
                output = *it;
            }
        }
    }
    return output;
}
void Directory::copyMe(BaseFile *OtherParent) {
    std::vector<BaseFile *>::iterator it;
    Directory* toAdd= new Directory(this->getName(),dynamic_cast<Directory*> (OtherParent));
    for (it = children.begin(); it != children.end(); ++it) {
        if (*it != nullptr) {
            (*it)->copyMe(toAdd);
        }
    }
    dynamic_cast<Directory*> (OtherParent)->addFile(toAdd);
}
void Directory::moveMe(BaseFile *MyParent, BaseFile *OtherParent) {
    parent=dynamic_cast<Directory*>(OtherParent);
    dynamic_cast<Directory*>(MyParent)->removeFile(this);
    dynamic_cast<Directory*>(OtherParent)->addFile(this);
}
void Directory::removeMe(BaseFile *ParentOfToRemove) {

    bool found=false;
    std::vector<BaseFile *>::iterator it;
    it = children.begin();

    while ((it != children.end())&!found) {

        if((*it) == ParentOfToRemove) {

            delete *it;
            children.erase(it);
            found=true;
        }

        ++it;




    }

}

