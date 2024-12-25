package org.smartboot.http;


import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.HttpResponse;
import tech.smartboot.feat.core.server.HttpServerHandler;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author 三刀
 * @version V1.0 , 2020/6/16
 */
public class SingleQueryHandler extends HttpServerHandler {
    private DataSource dataSource;

    public SingleQueryHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse response, CompletableFuture<Object> completableFuture) throws IOException {
        Thread.startVirtualThread(() -> {
            try {
                World world = new World();
                try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM World WHERE id=?");) {
                    preparedStatement.setInt(1, getRandomNumber());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    world.setId(resultSet.getInt(1));
                    world.setRandomNumber(resultSet.getInt(2));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                response.setContentType("application/json");
                JsonUtil.writeJsonBytes(response, world);
            } finally {
                completableFuture.complete(null);
            }
        });

    }

    protected int getRandomNumber() {
        return 1 + ThreadLocalRandom.current().nextInt(10000);
    }
}
