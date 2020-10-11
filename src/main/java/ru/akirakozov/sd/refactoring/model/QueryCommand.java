package ru.akirakozov.sd.refactoring.model;

public enum QueryCommand {
    MIN,
    MAX,
    SUM,
    COUNT,

    UNKNOWN;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
