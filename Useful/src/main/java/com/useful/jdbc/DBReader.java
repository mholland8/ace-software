package com.useful.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class DBReader implements ResultSetExtractor<DBRow>
{
	private final static String QUERY = "select * from film_category fc, film f where fc.film_id = f.film_id";
	
	public void setDataSource(DataSource dataSource)
	{
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void runQuery()
	{
		try
		{
			jdbcTemplate.query(QUERY, this);
		}
		catch(Exception ex)
		{
			System.out.println("Whaaaa");
			ex.printStackTrace();
		}
	}
	
	@Override
	public DBRow extractData(ResultSet rs) throws DataAccessException
	{
		try
		{
			while (rs.next())
			{
				String title = rs.getString("title");
				String description = rs.getString("description");
				
				System.out.println(String.format("%-45s %s", title, description));			
			}
		}
		catch(SQLException ex)
		{
			System.out.println("Whaaaa");
			ex.printStackTrace();
		}
	
		return null;
	}
	
	private JdbcTemplate jdbcTemplate;
}
