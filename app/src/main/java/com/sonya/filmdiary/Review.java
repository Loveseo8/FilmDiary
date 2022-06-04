package com.sonya.filmdiary;

public class Review {

    private String author, title, text;
    private int mark;

    public Review() {
    }

    public Review(String author, String title, String text, int mark) {
        this.author = author;
        this.title = title;
        this.text = text;
        this.mark = mark;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }
}
