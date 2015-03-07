package org.objectify.services

//object ResultWithNamedValue {
//
//  def failed(error: String, value: Any): Map[String, Any] = {
//    new ResultWithValue(value, error = Some(error))
//  }
//
//  def failed(error: String, value: (Any, Any)*): ResultWithValue = {
//    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), None, Some(error))
//  }
//
//  def ok(value: (Any, Any)*): ResultWithValue = {
//    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), None, None)
//  }
//
//  def ok(value: Any): ResultWithValue = {
//    new ResultWithValue(value, None, None)
//  }
//
//  def ok(value: Map[Any, Any]): ResultWithValue = {
//    new ResultWithValue(value, None, None)
//  }
//
//  def ok(message: String, value: (Any, Any)*): ResultWithValue = {
//    new ResultWithValue(value.foldLeft(Map[Any, Any]())((map, tuple) => map + tuple), Some(message), None)
//  }
//
//  def ok(message: String, value: Map[Any, Any]): ResultWithValue = {
//    ok(message, value)
//  }
//
//  def ok(message: String, value: Any): ResultWithValue = {
//    new ResultWithValue(value, Some(message), None)
//  }
//
//}

