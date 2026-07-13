package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.DashboardStatistics;

import java.sql.SQLException;

public interface DashboardDAO {
    DashboardStatistics loadStatistics() throws SQLException;
}
