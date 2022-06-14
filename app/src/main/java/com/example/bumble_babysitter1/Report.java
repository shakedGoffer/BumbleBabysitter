package com.example.bumble_babysitter1;


public class Report
{
    private String userName, babysitterUserName ,ReportContent;

    public Report(String userName, String ReportContent)
    {
        this.userName = userName;
        this.ReportContent = ReportContent;
    }

    public Report(String userName, String babysitterUserName, String ReportContent)
    {
        this.userName = userName;
        this.babysitterUserName = babysitterUserName;
        this.ReportContent = ReportContent;
    }

    public String getUserName() { return this.userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBabysitterUserName() { return this.babysitterUserName; }
    public void setBabysitterUserName(String babysitterUserName) { this.babysitterUserName = babysitterUserName; }

    public String getReportContent() { return this.ReportContent; }
    public void setReportContent(String ReportContent) { this.ReportContent = ReportContent; }
}
