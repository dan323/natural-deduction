package com.dan323.adapter;

import com.dan323.primaryports.Rule;
import com.dan323.secondaryports.RuleDao;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Stream;

public class CustomRuleDao {

    private static final Logger LOGGER = Loggers.getLogger(CustomRuleDao.class);

    public Flux<String> getRuleNames(String logic) {
        // input file
        Path ruleList = Paths.get("/" + logic + "/rules/list.txt");

        if (Files.exists(ruleList)){
            return Flux.using(
                    () -> Files.lines(ruleList),
                    Flux::fromStream,
                    Stream::close
            );
        } else {
            return Flux.empty();
        }
    }

    public Mono<Boolean> saveRule(String logic, Rule rule){
        Path rulePath = Paths.get("/" + logic + "/rules/"+rule+".txt");
        Path ruleList = Paths.get("/" + logic + "/rules/list.txt");

        boolean inList =false;
        try {
            Files.writeString(ruleList, System.lineSeparator() + rule.name(), StandardOpenOption.APPEND);
            inList = true;
            Files.writeString(rulePath, rule.toString(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return Mono.just(true);
        } catch (IOException e) {
            LOGGER.error("There was an error trying to save the rule.", e);
            if (inList){
                deleteLastLine(rulePath.getFileName().toString());
            }
            return Mono.just(false);
        }
    }

    private void deleteLastLine(String fileName) {
        try (RandomAccessFile f = new RandomAccessFile(fileName, "rw")){
            long length = f.length() - 1;
            byte b;
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
            } while(b != 10 && length > 0);
            if (length == 0) {
                f.setLength(length);
            } else {
                f.setLength(length + 1);
            }
        } catch (IOException e){
            LOGGER.error("Could not remove rule from list.");
        }
    }
}
