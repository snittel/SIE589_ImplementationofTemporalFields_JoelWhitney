from random import randint, random

__author__ = 'joelwhitney'
# SIE 558 - Final Take - Insert into DB
# this file:
# 1) opens connection to mysql db and sets up insert statement
# 2) set up pi serial connection
# 3) opens file to write results to and start reading from serial

# imports

import time
import datetime
import pymysql
import random

# 1) opens connection to mysql db and sets up insert statement
# open a connection to the database
cnx = pymysql.connect(host='localhost',
                      port=8889,
                      user='root',
                      passwd='root',
                      db='BlueberrySensors')
# SQL insert statement
insert_observation_query = ("INSERT INTO MoistureDataFAKE "
                            "(node, value, datetime, lat, lon) "
                            "VALUES (%s, %s, %s, %s, %s);")

# Sets up cursor object to interact with MYSQL connection
cursor = cnx.cursor()

time.sleep(2)

print("node, value, datetime, lat, lon")

# 3) opens file to write results to and start reading from serial
with open('arduinoOutput.txt', 'w') as f:
    while True:
        # assign the observation counter to id and value to value from the split string array
        node1 = str(1)
        value1 = str(random.uniform(8.0, 12.0))
        datetimestr1 = str(datetime.datetime.now())
        lat1 = str(44.704985)
        lon1 = str(-67.881473)
        query1 = "INSERT INTO MoistureDataFAKE (node, value, datetime, lat, lon) VALUES ('{}', '{}', '{}', '{}', '{}');".format(node1, value1, datetimestr1, lat1, lon1)
        tuple1 = node1 + ", " + value1 + ", " + datetimestr1 + ", " + lat1 + ", " + lon1
        # print response
        print(tuple1)
        # write tuple to file
        f.write(str(tuple1))
        # flush to make sure all writes are committed
        f.flush()
        # generate the query to insert the value into the SensorData table
        print("Query is: " + query1)
        print("\n" + "*" * 80)
        # ping the connection before cursor execution so the connection is re-opened if it went idle in downtime
        cnx.ping()
        # use execute function on cursor and insert data from arduino
        cursor.execute(query1)
        # make sure data is committed to the database before looping through again
        cnx.commit()

        # assign the observation counter to id and value to value from the split string array
        node2 = str(2)
        value2 = str(random.uniform(13.0, 17.0))
        datetimestr2 = str(datetime.datetime.now())
        lat2 = str(44.704985)
        lon2 = str(-67.881473)
        query2 = "INSERT INTO MoistureDataFAKE (node, value, datetime, lat, lon) VALUES ('{}', '{}', '{}', '{}', '{}')".format(node2, value2, datetimestr2, lat2, lon2)
        tuple2 = node2 + ", " + value2 + ", " + datetimestr2 + ", " + lat2 + ", " + lon2
        # print response
        print(tuple2)
        # write tuple to file
        f.write(str(tuple2))
        # flush to make sure all writes are committed
        f.flush()
        # generate the query to insert the value into the SensorData table
        print("Query is: " + query2)
        print("\n" + "*" * 80)
        # ping the connection before cursor execution so the connection is re-opened if it went idle in downtime
        cnx.ping()
        # use execute function on cursor and insert data from arduino
        cursor.execute(query2)
        # make sure data is committed to the database before looping through again
        cnx.commit()


        # assign the observation counter to id and value to value from the split string array
        node3 = str(3)
        value3 = str(random.uniform(20.0, 24.0))
        datetimestr3 = str(datetime.datetime.now())
        lat3 = str(44.704985)
        lon3 = str(-67.881473)
        query3 = "INSERT INTO MoistureDataFAKE (node, value, datetime, lat, lon) VALUES ('{}', '{}', '{}', '{}', '{}')".format(node3, value3, datetimestr3, lat3, lon3)
        tuple3 = node3 + ", " + value3 + ", " + datetimestr3 + ", " + lat3 + ", " + lon3
        # print response
        print(tuple3)
        # write tuple to file
        f.write(str(tuple3))
        # flush to make sure all writes are committed
        f.flush()
        # generate the query to insert the value into the SensorData table
        print("Query is: " + query3)
        print("\n" + "*" * 80)
        # ping the connection before cursor execution so the connection is re-opened if it went idle in downtime
        cnx.ping()
        # use execute function on cursor and insert data from arduino
        cursor.execute(query3)
        # make sure data is committed to the database before looping through again
        cnx.commit()

        time.sleep(20)
# close file, cursor, and connection when done writing
f.close()
cursor.close()
cnx.close()