package com.corpcare.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    private final DataSource dataSource;

    public DataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void monitorPool() {
        if (dataSource instanceof HikariDataSource hikari) {
            log.info("HikariCP pool '{}' initialised — maxPoolSize={}, minIdle={}",
                    hikari.getPoolName(), hikari.getMaximumPoolSize(), hikari.getMinimumIdle());
            startPoolMonitor(hikari);
        }
    }

    private void startPoolMonitor(HikariDataSource hikari) {
        Thread monitor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    int active = hikari.getHikariPoolMXBean().getActiveConnections();
                    int idle = hikari.getHikariPoolMXBean().getIdleConnections();
                    int pending = hikari.getHikariPoolMXBean().getThreadsAwaitingConnection();
                    int total = hikari.getHikariPoolMXBean().getTotalConnections();
                    if (active > 0 || pending > 0) {
                        log.warn("HikariCP [{}] active={}, idle={}, pending={}, total={}",
                                hikari.getPoolName(), active, idle, pending, total);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("HikariCP monitor error", e);
                }
            }
        }, "hikari-monitor");
        monitor.setDaemon(true);
        monitor.start();
    }
}
