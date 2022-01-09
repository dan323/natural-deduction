package com.dan323.adapter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class RuleSubscriber implements Subscriber<PairData> {

    private static final Logger LOGGER = Loggers.getLogger(CustomRuleDao.class);

    private final String logic;
    private Subscription subscription;
    private volatile boolean isWriting;
    private volatile String ruleName;
    private final MonoSink<PairData> sink;
    private final Path ruleList;

    public enum Action {
        LIST, RULE
    }

    public RuleSubscriber(String logic, MonoSink<PairData> monoSink) {
        this.logic = logic;
        this.sink = monoSink;
        this.ruleList = Paths.get(logic + "/rules/list.txt");
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(PairData data) {
        switch (data.getKey()) {
            case LIST -> {
                isWriting = true;
                try {
                    if (Files.exists(ruleList)) {
                        Files.writeString(ruleList, System.lineSeparator() + data.getValue(), StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(ruleList, data.getValue(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    }
                    LOGGER.info("Written in rule list.");
                    isWriting = false;
                    ruleName = data.getValue();
                    subscription.request(1);
                } catch (IOException e) {
                    sink.error(e);
                    subscription.cancel();
                }
            }
            case RULE -> {
                isWriting = true;
                final Path rulePath = Paths.get(logic + "/rules/" + ruleName + ".txt");
                try {
                    Files.writeString(rulePath, data.getValue(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    isWriting = false;
                    subscription.request(1);
                    LOGGER.info("Written in rule file.");
                } catch (IOException e) {
                    deleteLastLine(ruleList.getFileName().toString());
                    sink.error(e);
                    subscription.cancel();
                }
            }
        }
    }

    private void deleteLastLine(String fileName) {
        try (RandomAccessFile f = new RandomAccessFile(fileName, "rw")) {
            long length = f.length() - 1;
            byte b;
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
            } while (b != 10 && length > 0);
            if (length == 0) {
                f.setLength(length);
            } else {
                f.setLength(length + 1);
            }
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        subscription.cancel();
        sink.error(throwable);
    }

    @Override
    public void onComplete() {
        if (!isWriting) {
            sink.success();
        } else {
            sink.error(new IllegalStateException());
        }
    }
}
