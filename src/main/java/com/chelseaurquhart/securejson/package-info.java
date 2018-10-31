/**
 * Classes to securely translate to and from JSON by avoiding the use of strings which are too long-lived in memory, and
 * ensuring that the buffers that are potentially storing sensitive information are immediately erased after
 * consumption.
 *
 * @author Chelsea Urquhart
 */
package com.chelseaurquhart.securejson;
