package com.cqcloud.platform.exception;

import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2024年3月5日 🐬🐇 💓💕
 */
@NoArgsConstructor
public class BizException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public BizException(String msg) {
		super(msg);
	}

}
