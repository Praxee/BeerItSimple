package dataAccess;

import exception.*;
import model.News;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class NewsDBAccess implements NewsDataAccess {
    private Connection connection;

    public NewsDBAccess() throws ConnectionException {
        this.connection = DBConnection.getInstance();
    }

    public News getRandomNews() throws ThreadNewsException {
        News randomNews = null;
        String sqlInstruction = "SELECT * FROM news WHERE startingDate <= ? AND endDate >= ? ORDER BY RAND() LIMIT 1";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            ResultSet data = preparedStatement.executeQuery();

            if (data.next()) {
                randomNews = new News(
                        data.getInt("id"),
                        data.getString("title"),
                        data.getString("content"),
                        convertDateToGC(data.getTimestamp("startingDate")),
                        convertDateToGC(data.getTimestamp("endDate")),
                        data.getInt("employeeEntityId")
                );
            }
        } catch (SQLException e) {
            throw new ThreadNewsException();
        }
        return randomNews;
    }

    public ArrayList<News> getAllNews() throws DataQueryException {
        ArrayList<News> newsArrayList = new ArrayList<>();
        String sqlInstruction = "SELECT * FROM news ORDER BY endDate ASC, startingDate ASC";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            ResultSet data = preparedStatement.executeQuery();
            while (data.next()) {
                News news = new News(
                        data.getInt("id"),
                        data.getString("title"),
                        data.getString("content"),
                        convertDateToGC(data.getTimestamp("startingDate")),
                        convertDateToGC(data.getTimestamp("endDate")),
                        data.getInt("employeeEntityId")
                );

                newsArrayList.add(news);
            }
        } catch (SQLException e) {
            throw new DataQueryException();
        }
        return newsArrayList;
    }

    public News getNewsFromId(Integer id) throws NoRowSelected {
        String sqlInstruction = "SELECT * FROM news WHERE id = ?";
        News news = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setInt(1, id);
            ResultSet data = preparedStatement.executeQuery();
            if (data.next()) {
                news = new News(
                        data.getInt("id"),
                        data.getString("title"),
                        data.getString("content"),
                        convertDateToGC(data.getTimestamp("startingDate")),
                        convertDateToGC(data.getTimestamp("endDate")),
                        data.getInt("employeeEntityId")
                );
            }
        } catch (SQLException e) {
            throw new NoRowSelected();
        }

        return news;
    }

    private GregorianCalendar convertDateToGC(Timestamp date) {
        GregorianCalendar dateGC = null;
        if (date != null) {
            dateGC = new GregorianCalendar();
            dateGC.setTime(date);
        }
        return dateGC;
    }

    public void insertNews(News news) throws DataQueryException {
        String sqlInstruction = "INSERT INTO news(title, content, startingDate, endDate, EmployeeEntityId) VALUES(?,?,?,?,?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setString(1, news.getTitle());
            preparedStatement.setString(2, news.getContent());
            preparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(getLocalDateTime(news.getStartingDate())));
            preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(getLocalDateTime(news.getEndDate())));
            preparedStatement.setInt(5, news.getEmployeeEntityId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataQueryException();
        }
    }

    public boolean deleteNews(News news) throws DeletionException {
        String deleteInstruction = "DELETE FROM news WHERE id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteInstruction);
            preparedStatement.setInt(1, news.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DeletionException();
        }
        return true;
    }

    public void updateNews(News news) throws UpdateException {
        String sqlInstruction = "UPDATE news SET title = ?, content = ?, startingDate = ?, endDate = ? WHERE id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setString(1, news.getTitle());
            preparedStatement.setString(2, news.getContent());
            preparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(getLocalDateTime(news.getStartingDate())));
            preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(getLocalDateTime(news.getEndDate())));
            preparedStatement.setInt(5, news.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new UpdateException();
        }
    }

    private LocalDateTime getLocalDateTime(GregorianCalendar date) {
        return date.toZonedDateTime().toLocalDateTime();
    }
}
