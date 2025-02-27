# Part 2: Adding Road Network

## About
This repository contains the source code for the [Part 2 of the Supply Chain Tutorial](https://platform.amalgamasimulation.com/amalgama/SupplyChainTutorial/part2/sc_tutorial_part_2.html).

The application simulates the functionality of a simple supply chain.
A set of experiments is run to find the optimal number of trucks to move cargo among warehouses and stores.
Trucks move within a road network.

## How to build and run

1. Make sure that JDK-17+ and Maven 3.8.1+ are installed on your computer.
1. Clone the repository to your local machine.
1. Build the application: 

```
mvn clean package
```

4. Start the console application: 

```
java -jar target/sc-tutorial-part-2-1.0.jar
```

This gets printed to the console:

```
Scenario                Trucks count    SL      Expenses        Expenses/SL  
scenario.json                      1    57,14%  $ 420,00        $ 7,35
scenario1.1.json                   2    0,69%   $ 112 055,08    $ 162 683,60  
scenario1.2.json                   4    66,65%  $ 169 545,00    $ 2 543,97  
scenario1.3.json                   6    86,05%  $ 201 655,00    $ 2 343,60  
scenario2.json                     2    0,44%   $ 111 904,34    $ 255 461,63  
scenario3.json                     3    56,45%  $ 128 115,00    $ 2 269,71  
scenario4.json                    18    0,43%   $ 77 588,13     $ 181 556,22  
```
