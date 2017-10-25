package net.baruchans.hizen;

public class Section {
    private int begin;
    private int end;

    public Section(int begin, int end) {
        this.begin = begin;
        this.end   = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }
}
