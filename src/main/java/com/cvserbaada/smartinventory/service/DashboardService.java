package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.DashboardDAO;
import com.cvserbaada.smartinventory.dao.DashboardDAOImpl;
import com.cvserbaada.smartinventory.model.DashboardStatistics;

import java.sql.SQLException;

public class DashboardService {
    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this(new DashboardDAOImpl());
    }

    public DashboardService(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    public DashboardStatistics loadStatistics() throws SQLException {
        return dashboardDAO.loadStatistics();
    }
}
