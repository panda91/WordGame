# Word Scramble Game

## Prerequisites

You must install following to run application:
* Java 8
* Play Framework - activator
* [MongoDB](https://www.mongodb.com/download-center?jmp=docs#community)

Note: mongodb need to install in order to restore database for application.

## Installing Project

### Import mongo database

First, Open command prompt -> input following:
>`mongorestore --db word_scramble --collection words --drop --dir <bson files>`

Note: **<bson files>** here is path of bson file. For example: 
> `mongorestore --db word_scramble --collection words --drop --dir \db_resource\word_scramble\words.bson`

### Run application

Run the following command in currect project folder:
`activator run`
