# -*- coding: utf-8 -*-
"""
/***************************************************************************
 TemporalFieldGateway
                                 A QGIS plugin
 This allows users access the Temporal Field Java application running in a JVM
                              -------------------
        begin                : 2016-04-20
        git sha              : $Format:%H$
        copyright            : (C) 2016 by Joel Whitney
        email                : Joel.Whitney@maine.edu
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
"""
import PyQt4
import psycopg2
import subprocess
from PyQt4.QtCore import *
from PyQt4.QtCore import QSettings, QTranslator, qVersion, QCoreApplication
from PyQt4.QtGui import QAction, QIcon
# Initialize Qt resources from file resources.py
import resources
# Import the code for the dialog
from temporalField_dialog import TemporalFieldGatewayDialog
import os.path
from py4j.java_gateway import JavaGateway, GatewayParameters, launch_gateway
import pymysql
import datetime
from subprocess import *

class TemporalFieldGateway:
    """QGIS Plugin Implementation."""

    def __init__(self, iface):
        """Constructor.

        :param iface: An interface instance that will be passed to this class
            which provides the hook by which you can manipulate the QGIS
            application at run time.
        :type iface: QgsInterface
        """
        # Save reference to the QGIS interface
        self.iface = iface
        # initialize plugin directory
        self.plugin_dir = os.path.dirname(__file__)
        # initialize locale
        locale = QSettings().value('locale/userLocale')[0:2]
        locale_path = os.path.join(
            self.plugin_dir,
            'i18n',
            'TemporalFieldGateway_{}.qm'.format(locale))

        if os.path.exists(locale_path):
            self.translator = QTranslator()
            self.translator.load(locale_path)

            if qVersion() > '4.3.3':
                QCoreApplication.installTranslator(self.translator)

        # Create the dialog (after translation) and keep reference
        self.dlg = TemporalFieldGatewayDialog()

        # Declare instance attributes
        self.actions = []
        self.menu = self.tr(u'&Temporal Field Gateway')
        # TODO: We are going to let the user set this up in a future iteration
        self.toolbar = self.iface.addToolBar(u'TemporalFieldGateway')
        self.toolbar.setObjectName(u'TemporalFieldGateway')

    # noinspection PyMethodMayBeStatic
    def tr(self, message):
        """Get the translation for a string using Qt translation API.

        We implement this ourselves since we do not inherit QObject.

        :param message: String for translation.
        :type message: str, QString

        :returns: Translated version of message.
        :rtype: QString
        """
        # noinspection PyTypeChecker,PyArgumentList,PyCallByClass
        return QCoreApplication.translate('TemporalFieldGateway', message)


    def add_action(
        self,
        icon_path,
        text,
        callback,
        enabled_flag=True,
        add_to_menu=True,
        add_to_toolbar=True,
        status_tip=None,
        whats_this=None,
        parent=None):
        """Add a toolbar icon to the toolbar.

        :param icon_path: Path to the icon for this action. Can be a resource
            path (e.g. ':/plugins/foo/bar.png') or a normal file system path.
        :type icon_path: str

        :param text: Text that should be shown in menu items for this action.
        :type text: str

        :param callback: Function to be called when the action is triggered.
        :type callback: function

        :param enabled_flag: A flag indicating if the action should be enabled
            by default. Defaults to True.
        :type enabled_flag: bool

        :param add_to_menu: Flag indicating whether the action should also
            be added to the menu. Defaults to True.
        :type add_to_menu: bool

        :param add_to_toolbar: Flag indicating whether the action should also
            be added to the toolbar. Defaults to True.
        :type add_to_toolbar: bool

        :param status_tip: Optional text to show in a popup when mouse pointer
            hovers over the action.
        :type status_tip: str

        :param parent: Parent widget for the new action. Defaults None.
        :type parent: QWidget

        :param whats_this: Optional text to show in the status bar when the
            mouse pointer hovers over the action.

        :returns: The action that was created. Note that the action is also
            added to self.actions list.
        :rtype: QAction
        """

        icon = QIcon(icon_path)
        action = QAction(icon, text, parent)
        action.triggered.connect(callback)
        action.setEnabled(enabled_flag)

        if status_tip is not None:
            action.setStatusTip(status_tip)

        if whats_this is not None:
            action.setWhatsThis(whats_this)

        if add_to_toolbar:
            self.toolbar.addAction(action)

        if add_to_menu:
            self.iface.addPluginToMenu(
                self.menu,
                action)

        self.actions.append(action)

        return action

    def initGui(self):
        """Create the menu entries and toolbar icons inside the QGIS GUI."""

        icon_path = ':/plugins/TemporalFieldGateway/icon.png'
        self.add_action(
            icon_path,
            text=self.tr(u'Temporal Field Gateway'),
            callback=self.run,
            parent=self.iface.mainWindow())


    def unload(self):
        """Removes the plugin menu item and icon from QGIS GUI."""
        for action in self.actions:
            self.iface.removePluginMenu(
                self.tr(u'&Temporal Field Gateway'),
                action)
            self.iface.removeToolBarIcon(action)
        # remove the toolbar
        del self.toolbar

    def mySQLcnx(self, cnxString):
        try:
            cnx = pymysql.connect(host= cnxString['host'],
                                  port=cnxString['port'],
                                  user=cnxString['username'],
                                  passwd=cnxString['password'],
                                  db=cnxString['database'])

            # sets up cursor object to interact with MYSQL connection
            cursor = cnx.cursor()
            return cursor
        except (pymysql.ProgrammingError, pymysql.DataError, pymysql.IntegrityError, pymysql.NotSupportedError, pymysql.OperationalError), err:
            PyQt4.QtGui.QMessageBox.information(None, "DB Error",  "Something went wrong: {}".format(err))
            return False

    # if db type is postgresql use this
    def postgreSQLcnx(self, cnxString):
        try:
            cnx = psycopg2.connect(host=cnxString['host'],
                                  port=cnxString['port'],
                                  dbname=cnxString['database'],
                                  user=cnxString['username'],
                                  password=cnxString['password'])

            # sets up cursor object to interact with MYSQL connection
            cursor = cnx.cursor()
            return cursor
        except (psycopg2.ProgrammingError, psycopg2.DataError, psycopg2.IntegrityError, psycopg2.NotSupportedError, psycopg2.OperationalError), err:
            PyQt4.QtGui.QMessageBox.information(None, "DB Error", "Something went wrong: {}".format(err))
            return False

    # function for retrieving the start and end fields for the data
    def updateTempField_fields(self):
        # get connection info from plugin
        dbType, host, port, database, table, timefield, nodefield, node, username, password = str(self.dlg.dbType_comboBox.currentText()), self.dlg.host.text(), self.dlg.port.text(), self.dlg.dbName.text(), self.dlg.tableName.text(), self.dlg.timefieldName.text(), self.dlg.sIDfieldName.text(), self.dlg.sensorID.value(), self.dlg.username.text(), self.dlg.password.text()
        # create dictionary of connection info
        cnxString = {'dbType': str(dbType), 'host': str(host), 'port': int(port), 'database': str(database), 'table': str(table), 'timeField': str(timefield),'nodeField': str(nodefield),'node': str(node), 'username': str(username), 'password': str(password)}
        # determine where data is located and create appropriate cursor
        try:
            if dbType == 'PostgreSQL':
                cursor = self.postgreSQLcnx(cnxString)
            else:
                cursor = self.mySQLcnx(cnxString)
            # SQL query to know to start of Temp Field
            startTimeSQL = "SELECT MIN(" + cnxString['timeField'] + ") AS startTime FROM " + cnxString['table'] + " WHERE " + cnxString['nodeField'] + " = '" + cnxString['node']+ "';"
            cursor.execute(startTimeSQL)
            for line in cursor:
                startTime, queryTime = str(line[0]), str(line[0])
            qtStart = PyQt4.QtCore.QDateTime.fromString(startTime, 'yyyy-MM-dd HH:mm:ss')
            qtQuery = PyQt4.QtCore.QDateTime.fromString(queryTime, 'yyyy-MM-dd HH:mm:ss')
            self.dlg.startTime.setDateTime(qtStart)
            self.dlg.queryTime.setDateTime(qtQuery)
            # SQL query to know to end of Temp Field
            endTimeSQL = "SELECT MAX(" + cnxString['timeField'] + ") AS endTime FROM " + cnxString['table'] + " WHERE " + cnxString['nodeField'] + " = '" + cnxString['node']+ "';"
            cursor.execute(endTimeSQL)
            for line in cursor:
                endTime = str(line[0])
            qtEnd = PyQt4.QtCore.QDateTime.fromString(endTime, 'yyyy-MM-dd HH:mm:ss')
            self.dlg.endTime.setDateTime(qtEnd)
        except:
            pass

    def run(self):
        """Run method that performs all the real work"""
        #os.system("java -jar C:/Users/Joel/Desktop/TemporalFieldGateway.jar MySQL host;localhost,port;8889,database;BlueberrySensors,table;moisturedata,username;root,password;root")

        #subprocess.call(['java', '-jar', 'C:/Users/Joel/Desktop/TemporalFieldGateway.jar'])

        #path = r"C:/Users/Joel/.qgis2/python/plugins/TemporalFieldGateway/TemporalFieldGateway.jar"
        #subprocess.Popen(['java', '-jar', '"' + path + '"'], shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        # args = [r"C://Users//Joel//.qgis2//python//plugins//TemporalFieldGateway//TemporalFieldGateway.jar", "dbtype;PostgreSQL,host;localhost,port;5432,database;blueberrysensors,table;moisturedata,username;joelw,password;999jbw"]  # Any number of args to be passed to the jar file
        # Popen(['java', '-jar', args[0], args[1]], shell=True)
            # Py4JNetworkError: An error occurred while trying to connect to the Java server (127.0.0.1:25335)
            # IF I TAKE OUT SHELL ARGUMENT "Can't find file at specified location" error

        # using jpype - start the JVM with the good classpaths
        # classpath = "C://Users//Joel//.qgis2//python//plugins//TemporalFieldGateway//TemporalFieldGateway.jar"
        # args = "dbtype;PostgreSQL,host;localhost,port;5432,database;blueberrysensors,table;moisturedata,username;joelw,password;999jbw"
        # jpype.startJVM(jpype.getDefaultJVMPath(), "-Djava.class.path={};{}".format(classpath, args))
            # unable to install jpype successfully

        # using py4j.launchgateway
        # launch_gateway(port=25335, classpath=r"C://Users//Joel//Desktop//TemporalFieldGateway.jar", javaopts=["dbtype;PostgreSQL,host;localhost,port;5432,database;blueberrysensors,table;moisturedata,username;joelw,password;999jbw"])
            # WindowsError: [Error 2] The system cannot find the file specified

        # connect dataBounds button to update fields function
        self.dlg.dataBounds.clicked.connect(self.updateTempField_fields)

        # show the dialog
        self.dlg.show()
        # Run the dialog event loop
        result = self.dlg.exec_()
        # See if OK was pressed
        if result:
            # temporal field stuff
            startTime = str(self.dlg.startTime.dateTime().toString("yyyy-MM-dd HH:mm:ss"))
            endTime = str(self.dlg.endTime.dateTime().toString("yyyy-MM-dd HH:mm:ss"))
            sensorID = str(self.dlg.sensorID.value())
            queryTime = str(self.dlg.queryTime.dateTime().toString("yyyy-MM-dd HH:mm:ss"))

            # create connection to running java program
            gateway = JavaGateway(gateway_parameters=GatewayParameters(port=25335))  # connect to the JVM
            #gateway = JavaGateway.launch_gateway(jarpath=r'C:\Users\Joel\.qgis2\python\plugins\TemporalFieldGateway', classpath='TemporalFieldGateway.jar')
            temporalFieldApp = gateway.entry_point  # get the AdditionApplication instance

            if self.dlg.methodTabs.currentIndex() == 0:
                method = str(self.dlg.method_comboBox.currentText())
                # do something based on method selected
                if method == 'getValue()':
                    value = temporalFieldApp.getValue(startTime, endTime, sensorID, queryTime)  # call the addition method
                elif method == 'getAvg()':
                    value = temporalFieldApp.getAvg(startTime, endTime, sensorID, queryTime)  # call the addition method
                elif method == 'getMin()':
                    value = temporalFieldApp.getMin(startTime, endTime, sensorID, queryTime)  # call the addition method
                elif method == 'getMax()':
                    value = temporalFieldApp.getMax(startTime, endTime, sensorID, queryTime)  # call the addition method
                PyQt4.QtGui.QMessageBox.information(None, "Results",  'Method: ' + str(method) + '\nTemporal Field: ' + "(sensorID) '" + sensorID + "' (queryTime) '" + queryTime + "'\nValue: " + str(value))
            else:
                plotInterval, interval, outputCSV, outFolder, plotPoints = str(self.dlg.plotInterval.isChecked()), int(self.dlg.interval.value()), str(self.dlg.outputCSV.isChecked()), str(self.dlg.outFolder.text()), str(self.dlg.plotPoints.isChecked())
                temporalFieldApp.interpolateIntervals(startTime, endTime, sensorID, plotInterval, interval, outputCSV, outFolder, plotPoints)




