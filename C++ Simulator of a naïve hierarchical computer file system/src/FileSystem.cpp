//
// Created by yuval on 09/11/17.
//

#include "../include/FileSystem.h"
#include <iostream>
using namespace std;

FileSystem::FileSystem() : rootDirectory(new Directory("/", nullptr)) , workingDirectory(rootDirectory) {


}
FileSystem::~FileSystem() {delete rootDirectory;}
Directory& FileSystem::getRootDirectory() const {

    return *rootDirectory;}
Directory& FileSystem::getWorkingDirectory() const { return  *workingDirectory;}
void FileSystem::setWorkingDirectory(Directory *newWorkingDirectory) {
workingDirectory = newWorkingDirectory;
}


//START RULE OF FIVE
FileSystem::FileSystem(FileSystem &&other): rootDirectory(new Directory("/", nullptr)) , workingDirectory(rootDirectory) {
    rootDirectory = &other.getRootDirectory();
    workingDirectory = &other.getWorkingDirectory();
    rootDirectory= nullptr;
    workingDirectory= nullptr;
}
FileSystem::FileSystem(const FileSystem &other): rootDirectory(new Directory("/", nullptr)) , workingDirectory(rootDirectory) {
    rootDirectory = new Directory(*(&other.getRootDirectory()));
    workingDirectory = new Directory(*(&other.getWorkingDirectory()));
}
FileSystem& FileSystem::operator=(FileSystem &&other) {
    if (this != &other)
    {
        delete rootDirectory;
        delete workingDirectory;// deallocate
        rootDirectory = &other.getRootDirectory();
        workingDirectory = &other.getWorkingDirectory();
        rootDirectory= nullptr;
        workingDirectory= nullptr;
    }

    return *this;
}
FileSystem& FileSystem::operator=(const FileSystem &other) {
    if (this != &other)
    {
        delete rootDirectory;
        delete workingDirectory;// deallocate
        rootDirectory = new Directory(*(&other.getRootDirectory()));
        workingDirectory = new Directory(*(&other.getWorkingDirectory()));
    }

    return *this;}
//END RULE OF FIVE
