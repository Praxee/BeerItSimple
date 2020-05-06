package dataAccess;

import exception.CustomerException;
import exception.DuplicataException;
import exception.NoCustomerFoundException;
import model.City;
import model.Customer;
import model.Entity;
import model.Rank;

import java.sql.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CustomerDBAccess implements CustomerDataAccess{
    private Connection connection;

    public CustomerDBAccess() {
        this.connection = DBConnection.getDBConnection();
    }

    @Override
    public ArrayList<Customer> getAllCustomers() {
        String sqlInstruction = "SELECT customer.*, e.*, r.*, city.*\n" +
                                "FROM customer JOIN entity e ON customer.EntityId = e.id\n" +
                                "JOIN `rank` r ON r .id = customer.RankId\n" +
                                "JOIN city ON e.CityLabel = city.label AND e.CityZipCode = city.zipCode";
        ArrayList<Customer> customerList;
        try {
            ResultSet data = connection.createStatement().executeQuery(sqlInstruction);

            customerList = new ArrayList<>();
            Customer customer;
            Entity entity;
            City city;
            Rank rank;

            String mail;
            String VATNumber;
            GregorianCalendar calendar = null;
            String accountNumber;
            String businessNumber;


            while(data.next()) {
                city = new City(
                        data.getString("CityLabel"),
                        data.getInt("CityZipCode")
                );

                entity = new Entity(
                        data.getInt("id"),
                        data.getString("contactName"),
                        data.getString("phoneNumber"),
                        data.getInt("houseNumber"),
                        data.getString("street"),
                        city
                );

                accountNumber = data.getString("bankAccountNumber");
                if (accountNumber != null)
                    entity.setBankAccountNumber(accountNumber);

                businessNumber = data.getString("businessNumber");
                if (businessNumber != null)
                    entity.setBusinessNumber(businessNumber);


                rank = new Rank(
                        data.getInt("r.id"),
                        data.getString("r.label"),
                        data.getInt("r.creditLimit")
                );

                mail = data.getString("mail");
                if (!data.wasNull()) {
                    entity.setMail(mail);
                }

                java.sql.Date subscriptionDate = data.getDate("subscriptionDate");
                if (!data.wasNull()) {
                    calendar = new GregorianCalendar();
                    calendar.setTime(subscriptionDate);
                }

                customer = new Customer(entity, rank, calendar);

                customerList.add(customer);
            }

            return customerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Customer getCustomer(Integer id) throws CustomerException, NoCustomerFoundException {
        String sqlInstruction = "SELECT customer.*, entity.*, r.*, city.* FROM customer\n" +
                                "JOIN entity ON customer.EntityId = entity.id\n" +
                                "JOIN `rank` r ON r.id = customer.RankId\n" +
                                "JOIN city ON entity.cityLabel = city.label AND entity.CityZipCode = city.zipCode\n" +
                                "WHERE customer.EntityId = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setInt(1, id);

            ResultSet data = preparedStatement.executeQuery();

            if (data.next()) {
                City city = new City(
                        data.getString("CityLabel"),
                        data.getInt("CityZipCode")
                );

                Entity entity = new Entity(
                        data.getInt("id"),
                        data.getString("contactName"),
                        data.getString("phoneNumber"),
                        data.getInt("houseNumber"),
                        data.getString("street"),
                        city
                );

                String bankAccount = data.getString("bankAccountNumber");
                if (bankAccount != null) {
                    entity.setBankAccountNumber(bankAccount);
                }

                String businessNumber = data.getString("businessNumber");
                if (businessNumber != null) {
                    entity.setBusinessNumber(businessNumber);
                }

                Rank rank = new Rank(
                        data.getInt("r.id"),
                        data.getString("r.label"),
                        data.getInt("r.creditLimit")
                );

                String mail = data.getString("mail");
                if (!data.wasNull()) {
                    entity.setMail(mail);
                }

                GregorianCalendar calendar = null;
                java.sql.Date subscriptionDate = data.getDate("subscriptionDate");
                if (!data.wasNull()) {
                    calendar = new GregorianCalendar();
                    calendar.setTime(subscriptionDate);
                }

                return new Customer(entity, rank, calendar);

            } else {{
                throw new NoCustomerFoundException();
            }}
        } catch (SQLException e) {
            throw new CustomerException();
        }
    }

    public boolean create(Customer c) throws SQLException {
        int affectedRowsEntity = 0;
        int affectedRowsCustomer = 0;
        int entityID = 0;
        PreparedStatement preparedStatementEntity = null;

        //Entity
        try {
            String sqlEntity = "INSERT INTO entity(mail,contactName, phoneNumber, houseNumber, street, bankAccountNumber, businessNumber, Citylabel, CityZipCode) values(?,?,?,?,?,?,?,?,?)";
            Entity entity = c.getEntity();
            City city = c.getEntity().getCity();
            preparedStatementEntity = connection.prepareStatement(sqlEntity, Statement.RETURN_GENERATED_KEYS);
            preparedStatementEntity.setString(1, entity.getMail());
            preparedStatementEntity.setString(2, entity.getContactName());
            preparedStatementEntity.setString(3, entity.getPhoneNumber());
            preparedStatementEntity.setInt(4, entity.getHouseNumber());
            preparedStatementEntity.setString(5, entity.getStreet());
            preparedStatementEntity.setString(6, entity.getBankAccountNumber());
            preparedStatementEntity.setString(7, entity.getBusinessNumber());
            preparedStatementEntity.setString(8, city.getLabel());
            preparedStatementEntity.setInt(9, city.getZipCode());
            affectedRowsEntity = preparedStatementEntity.executeUpdate();
        }catch(SQLException ex) {
                throw ex;
        }
        //Customer
        if(affectedRowsEntity != 0) {
            try (ResultSet generatedKeys = preparedStatementEntity.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    String sqlCustomer = "INSERT INTO customer(`EntityId`,`RankId`,`subscriptionDate`) values(?,?,?)";
                    PreparedStatement preparedStatementCustomer = connection.prepareStatement(sqlCustomer);
                    entityID = generatedKeys.getInt(1);
                    preparedStatementCustomer.setInt(1, entityID);
                    preparedStatementCustomer.setInt(2, c.getRank().getId());
                    preparedStatementCustomer.setDate(3, Date.valueOf(java.time.LocalDate.now()));
                    affectedRowsCustomer = preparedStatementCustomer.executeUpdate();


                } else {
                    throw new SQLException("No ID obtained from entity.");
                }
            } catch(SQLException ex){
                String sqlEntityDel = "DELETE FROM entity WHERE id = ?";
                PreparedStatement preparedStatementEntityDel = connection.prepareStatement(sqlEntityDel);
                preparedStatementEntityDel.setInt(1, entityID);
                preparedStatementEntityDel.executeUpdate();
            }
        } else {
            throw new SQLException("Creating Entity failed.");
        }
        return affectedRowsCustomer != 0;
    }

    public boolean update(Customer customer) throws DuplicataException {
        int affectedRow = 0;

        String sqlInstruction = "UPDATE customer\n" +
                                "JOIN entity ON customer.EntityId = entity.id\n" +
                                "SET\n" +
                                " customer.RankId = ?,\n" +
                                " entity.mail = ?,\n" +
                                " entity.contactName = ?,\n" +
                                "entity.phoneNumber = ?,\n" +
                                "entity.houseNumber = ?,\n" +
                                "entity.street = ?,\n" +
                                "entity.bankAccountNumber = ?,\n" +
                                "entity.businessNumber = ?,\n" +
                                "entity.CityLabel = ?,\n" +
                                "entity.CityZipCode = ?\n" +
                                "WHERE customer.EntityId = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            preparedStatement.setInt(1, customer.getRank().getId());
            preparedStatement.setString(2, customer.getEntity().getMail());
            preparedStatement.setString(3, customer.getEntity().getContactName());
            preparedStatement.setString(4, customer.getEntity().getPhoneNumber());
            preparedStatement.setInt(5, customer.getEntity().getHouseNumber());
            preparedStatement.setString(6, customer.getEntity().getStreet());
            preparedStatement.setString(7, customer.getEntity().getBankAccountNumber());
            preparedStatement.setString(8, customer.getEntity().getBusinessNumber());
            preparedStatement.setString(9, customer.getEntity().getCity().getLabel());
            preparedStatement.setInt(10, customer.getEntity().getCity().getZipCode());
            preparedStatement.setInt(11, customer.getEntity().getId());

            preparedStatement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicataException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean delete(Customer customer) {

        String sqlUpdateOrder = "UPDATE `order` SET CustomerEntityid = null\n" +
                                "WHERE CustomerEntityid = ?";

        try {
            PreparedStatement preparedStatementOrder = connection.prepareStatement(sqlUpdateOrder);
            preparedStatementOrder.setInt(1, customer.getEntity().getId());

            preparedStatementOrder.executeUpdate();

            String sqlDeleteCustomer = "DELETE FROM customer\n" +
                                    "WHERE EntityId = ?";

            PreparedStatement preparedStatementDeleteCustomer = connection.prepareStatement(sqlDeleteCustomer);
            preparedStatementDeleteCustomer.setInt(1, customer.getEntity().getId());
            preparedStatementDeleteCustomer.executeUpdate();

            String sqlDeleteEntity = "DELETE FROM entity\n" +
                                    "WHERE id = ? AND id NOT IN (SELECT EntityId FROM employee)";

            PreparedStatement preparedStatementDeleteEntity = connection.prepareStatement(sqlDeleteEntity);
            preparedStatementDeleteEntity.setInt(1, customer.getEntity().getId());
            preparedStatementDeleteEntity.executeUpdate();
        } catch (SQLException e) {
//            try {
//            connection.prepareStatement("rollback ;").executeUpdate();
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//            }
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
