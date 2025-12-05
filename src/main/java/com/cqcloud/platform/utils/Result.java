package com.cqcloud.platform.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 响应信息主体
 *
 * @author weimeilayer@gmail.com ✨
 * @date 2024年3月5日 🐬🐇
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "响应信息主体")
public class Result<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	// 标准状态码常量
	public static final int SUCCESS_CODE = 200;

	public static final int FAILURE_CODE = 400;

	public static final int ERROR_CODE = 500;

	public static final int UNAUTHORIZED_CODE = 401;

	public static final int FORBIDDEN_CODE = 403;

	public static final int NOT_FOUND_CODE = 404;

	@Schema(description = "返回标记: 200=success, 400=failure, 500=error", example = "200")
	private Integer code;

	@Schema(description = "返回信息")
	private String msg;

	@Schema(description = "数据")
	private T data;

	public static <T> Result<T> ok() {
		return of(SUCCESS_CODE, null, null);
	}

	public static <T> Result<T> ok(T data) {
		return of(SUCCESS_CODE, null, data);
	}

	public static <T> Result<T> ok(T data, String msg) {
		return of(SUCCESS_CODE, msg, data);
	}

	public static <T> Result<T> success(String msg) {
		return of(SUCCESS_CODE, msg, null);
	}

	public static <T> Result<T> fail() {
		return of(FAILURE_CODE, null, null);
	}

	public static <T> Result<T> fail(String msg) {
		return of(FAILURE_CODE, msg, null);
	}

	public static <T> Result<T> fail(T data, String msg) {
		return of(FAILURE_CODE, msg, data);
	}

	public static <T> Result<T> error() {
		return of(ERROR_CODE, null, null);
	}

	public static <T> Result<T> error(String msg) {
		return of(ERROR_CODE, msg, null);
	}

	public static <T> Result<T> error(T data, String msg) {
		return of(ERROR_CODE, msg, data);
	}

	public static <T> Result<T> unauthorized(String msg) {
		return of(UNAUTHORIZED_CODE, msg, null);
	}

	public static <T> Result<T> forbidden(String msg) {
		return of(FORBIDDEN_CODE, msg, null);
	}

	public static <T> Result<T> notFound(String msg) {
		return of(NOT_FOUND_CODE, msg, null);
	}

	public static <T> Result<T> of(int code, String msg, T data) {
		Result<T> result = new Result<>();
		result.setCode(code);
		result.setMsg(msg);
		result.setData(data);
		return result;
	}

	public boolean isSuccess() {
		return Objects.equals(code, SUCCESS_CODE);
	}

	public boolean isFailure() {
		return Objects.equals(code, FAILURE_CODE);
	}

	public boolean isError() {
		return Objects.equals(code, ERROR_CODE);
	}

}