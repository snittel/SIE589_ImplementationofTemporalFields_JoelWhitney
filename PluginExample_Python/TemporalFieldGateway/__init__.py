# -*- coding: utf-8 -*-
"""
/***************************************************************************
 TemporalFieldGateway
                                 A QGIS plugin
 This allows users access the Temporal Field Java application running in a JVM
                             -------------------
        begin                : 2016-04-20
        copyright            : (C) 2016 by Joel Whitney
        email                : Joel.Whitney@maine.edu
        git sha              : $Format:%H$
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
 This script initializes the plugin, making it known to QGIS.
"""


# noinspection PyPep8Naming
def classFactory(iface):  # pylint: disable=invalid-name
    """Load TemporalFieldGateway class from file TemporalFieldGateway.

    :param iface: A QGIS interface instance.
    :type iface: QgsInterface
    """
    #
    from .temporalField import TemporalFieldGateway
    return TemporalFieldGateway(iface)
