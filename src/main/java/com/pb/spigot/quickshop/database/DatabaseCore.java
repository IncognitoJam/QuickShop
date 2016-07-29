package com.pb.spigot.quickshop.database;

import java.sql.Connection;

public interface DatabaseCore {

    Connection getConnection();

    void queue(BufferStatement bs);

    void flush();

    void close();

}