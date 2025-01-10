package com.transit.configuration;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class HadoopConfiguration {

    @Value("${hadoop.fs.uri}")
    private String hdfsUri;

    @Value("${hadoop.user}")
    private String hdfsUser;

    @Bean
    public FileSystem fileSystem() throws IOException, InterruptedException, URISyntaxException {
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("fs.defaultFS", hdfsUri);
        return FileSystem.get(new URI(hdfsUri), conf, hdfsUser);
    }
}
