package com.luckia.biller.core.common;

/**
 * Nodo de un AST (Abstract Syntax Tree) utilizado para parsear las expresiones FIQL.
 */
public interface ASTNode<T> {

	T build();
}
