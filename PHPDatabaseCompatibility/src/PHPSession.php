<?php

interface PHPSession {
	
	/**
	 * Returns this session's id.
	 *
	 * @return
	 */
	public function getId();
	
// 	/**
// 	 * Returns the id of the last statement used by this session.
// 	 *
// 	 * @return the id of the last statement used by this session
// 	*/
// 	public function getLastStatementId();
	
	/**
	 * Returns the id of the last prepared statement used by this session.
	 *
	 * @return the id of the last prepared statement used by this session
	*/
	public function getLastPreparedStatementId();
	
// 	/**
// 	 * Returns the id of the last result set used by this session.
// 	 *
// 	 * @return the id of the last result set used by this session
// 	*/
// 	public function getLastResultSetId();
	
	/**
	 * Returns the connection associated with this session.
	 *
	 * @return the connection associated with this session
	*/
	public function getConnection();
	
// 	/**
// 	 * Creates a new statement with the specified id. If the
// 	 * <code>id</code> is null, it is assumed to be the last statement id used
// 	 * with this session.
// 	 *
// 	 * @param id
// 	 * @return
// 	 * @throws SQLException
// 	 */
// 	public function createStatement($id);
	
// 	/**
// 	 * Retrieves the statement associated with the specified id. If the
// 	 * <code>id</code> is null, it is assumed to be the last statement id used
// 	 * with this session.
// 	 *
// 	 * @param id
// 	 * @return
// 	 */
// 	public function getStatement($id);
	
// 	/**
// 	 * Manually adds a JDBC statement to the map of those associated with this
// 	 * session. If the <code>id</code> is <code>null</code>, it is assumed to be
// 	 * the last statement id used with this session.
// 	 *
// 	 * @param id
// 	 * @param s
// 	 * @return
// 	*/
	
// 	public function putStatement($id, $s);
	
// 	/**
// 	 * Removes a statement from the map of those associated with this
// 	 * session. If the <code>id</code> is <code>null</code>, it is assumed to be
// 	 * the last statement id used with this session.
// 	 *
// 	 * @param id
// 	 * the statement to remove from this session
// 	 * @return the statement that was removed or <code>null</code> if there was
// 	 * no statement with the specified id
// 	*/
// 	public function removeStatement($id);
	
	/**
	 * Returns the PreparedStatement associated with the specified id.
	 *
	 * @param id
	 * @return
	*/
	public function getPreparedStatement($id);
	public function putPreparedStatement($id, $ps);
	
	/**
	 * Dissociates the prepared statement referenced by the specified id from
	 * this session. This automatically closes the prepared statement.
	 *
	 * @param id
	 * the prepared statement to remove from this session
	 * @return the prepared statement that was removed or <code>null</code> if
	 * there was no prepared statement with the specified id
	*/
	public function removePreparedStatement($id);
	
// 	/**
// 	 * Returns the result set associated with the specified id.
// 	 *
// 	 * @param id
// 	 * @return
// 	*/
// 	public function getResultSet($id);
// 	public function putResultSet($id, $resultSet);
	
// 	/**
// 	 * Dissociates the result set referenced by the specified id from this
// 	 * session. This automatically closes the result set.
// 	 *
// 	 * @param id
// 	 * the result set to remove from this session
// 	 * @return the result set that was removed or <code>null</code> if there was
// 	 * no result set with the specified id
// 	*/
// 	public function removeResultSet($id);
}
?>