package nextstep.subway.line;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.common.dto.ErrorResponse;
import nextstep.subway.common.exception.NotFoundException;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineAcceptanceTest extends AcceptanceTest {
	Long 강남역_id;
	Long 광교역_id;
	int 강남역_광교역_거리 = 0;

	@BeforeEach
	void setUpLineTest() {
		강남역_id = createStation("강남역");
		광교역_id = createStation("광교역");
		강남역_광교역_거리 = 10;
	}

	@DisplayName("지하철 노선을 생성한다.")
	@Test
	void createLine() {
		// when
		ExtractableResponse<Response> response = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// then
		지하철_노선_생성됨(response);
	}

	@DisplayName("지하철 노선을 생성시 종점역을 추가한다.")
	@Test
	void createLineWithSection() {
		// when
		ExtractableResponse<Response> response = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// then
		지하철_노선_생성됨(response);
	}

	@DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
	@Test
	void createLine2() {
		// given
		// 지하철_노선_등록되어_있음
		지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// when
		ExtractableResponse<Response> response = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// then
		지하철_노선_생성_실패됨(response);
	}

	@DisplayName("지하철 노선 목록을 조회한다.")
	@Test
	void getLines() {
		// given
		// 지하철_노선_등록되어_있음
		ExtractableResponse<Response> createdLine1 = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// 지하철_노선_등록되어_있음
		Long 신도림역_id = createStation("신도림역");
		Long 건대입구역_id = createStation("건대입구역");
		int 신도림역_건대입구역_거리 = 20;
		ExtractableResponse<Response> createdLine2 = 지하철_노선_생성_요청("2호선", "bg-green-600", 신도림역_id, 건대입구역_id,
			신도림역_건대입구역_거리);

		// when
		ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();
		// then
		지하철_노선_목록_응답됨(response);
		지하철_노선_목록_포함됨(response, createdLine1, createdLine2);
	}

	@DisplayName("지하철 노선을 조회한다.")
	@Test
	void getLine() {
		// given
		// 지하철_노선_등록되어_있음
		ExtractableResponse<Response> createdLine = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// when
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(createdLine);
		// then
		지하철_노선_응답됨(createdLine, response);
	}

	@DisplayName("존재하지 않는 노선을 조회할 경우 에러가 발생한다.")
	@Test
	void getNotFoundLine() {
		// when
		ExtractableResponse<Response> response = 존재하지_않는_지하철_노선_조회_요청();
		// then
		지하철_노선_존재하지_않음(response);
	}

	@DisplayName("지하철 노선을 수정한다.")
	@Test
	void updateLine() {
		// given
		// 지하철_노선_등록되어_있음
		ExtractableResponse<Response> beforeLine = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// when
		ExtractableResponse<Response> response = 지하철_노선_수정_요청(beforeLine, "2호선", "bg-green-600");
		// then
		지하철_노선_수정됨(beforeLine, response);
	}

	@DisplayName("존재하지 않는 노선을 수정할 경우 에러가 발생한다.")
	@Test
	void updateLineFailWhenLineNotExist() {
		// when
		ExtractableResponse<Response> response = 존재하지_않는_지하철_수정_요청("신분당선", "bg-red-600");
		// then
		지하철_노선_존재하지_않음(response);
	}

	@DisplayName("지하철 노선을 제거한다.")
	@Test
	void deleteLine() {
		// given
		// 지하철_노선_등록되어_있음
		ExtractableResponse<Response> line = 지하철_노선_생성_요청("신분당선", "bg-red-600", 강남역_id, 광교역_id, 강남역_광교역_거리);
		// when
		ExtractableResponse<Response> response = 지하철_노선_제거_요청(line);
		// then
		지하철_노선_삭제됨(response);
	}

	private void 지하철_노선_생성됨(final ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(response.header("Location")).isNotBlank();
	}

	private void 지하철_노선_생성_실패됨(final ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	public static void 지하철_노선_목록_포함됨(final ExtractableResponse<Response> response,
		final ExtractableResponse<Response>... createdLine) {

		List<Long> savedLineIds = Arrays.stream(createdLine).
			map(line -> getLineId(line))
			.collect(Collectors.toList());

		List<Long> actualLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
			.map(LineResponse::getId)
			.collect(Collectors.toList());
		assertThat(actualLineIds).containsAll(savedLineIds);
	}

	private static Long getLineId(final ExtractableResponse<Response> line) {
		return Long.parseLong(line.header("Location").split("/")[2]);
	}

	private void 지하철_노선_목록_응답됨(final ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}

	public static ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
		// when
		return RestAssured.given().log().all()
			.when().get("/lines")
			.then().log().all().extract();
	}

	private void 지하철_노선_응답됨(final ExtractableResponse<Response> createdLine,
		final ExtractableResponse<Response> response) {
		Long lineId = getLineId(createdLine);

		Long expectedId = response.jsonPath().getObject(".", LineResponse.class).getId();
		assertThat(lineId).isEqualTo(expectedId);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}

	public static ExtractableResponse<Response> 지하철_노선_조회_요청(final ExtractableResponse<Response> createdLine) {
		Long lineId = getLineId(createdLine);
		return 지하철_노선_조회_요청(lineId);
	}

	public static ExtractableResponse<Response> 지하철_노선_조회_요청(Long lineId) {
		return RestAssured.given().log().all()
			.when().get("/lines/{id}", lineId)
			.then().log().all().extract();
	}

	private void 지하철_노선_수정됨(final ExtractableResponse<Response> beforeLine,
		final ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}

	private ExtractableResponse<Response> 지하철_노선_수정_요청(final ExtractableResponse<Response> createdLine, String name,
		String color) {
		Long lineId = getLineId(createdLine);
		Map<String, String> params = new HashMap<>();
		params.put("name", name);
		params.put("color", color);

		return RestAssured.given().log().all()
			.body(params)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.put("/lines/{id}", lineId)
			.then().log().all()
			.extract();
	}

	private void 지하철_노선_삭제됨(final ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	private ExtractableResponse<Response> 지하철_노선_제거_요청(final ExtractableResponse<Response> line) {
		Long lineId = getLineId(line);
		return RestAssured.given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.delete("/lines/{id}", lineId)
			.then().log().all()
			.extract();
	}

	private void 지하철_노선_존재하지_않음(ExtractableResponse<Response> response) {
		String errorCode = response.jsonPath().getObject(".", ErrorResponse.class).getErrorCode();
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(errorCode).isEqualTo(NotFoundException.ERROR_CODE);
	}

	private ExtractableResponse<Response> 존재하지_않는_지하철_노선_조회_요청() {
		Long lineId = 10L;
		return RestAssured.given().log().all()
			.when().get("/lines/{id}", lineId)
			.then().log().all().extract();
	}

	private ExtractableResponse<Response> 존재하지_않는_지하철_수정_요청(String name, String color) {
		Long lineId = 10L;

		Map<String, String> params = new HashMap<>();
		params.put("name", name);
		params.put("color", color);

		return RestAssured.given().log().all()
			.body(params)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.put("/lines/{id}", lineId)
			.then().log().all()
			.extract();
	}

	private static Long createStation(String name) {
		Map<String, String> params = new HashMap<>();
		params.put("name", name);
		ExtractableResponse<Response> response = RestAssured.given().log().all()
			.body(params)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("/stations")
			.then().log().all()
			.extract();

		return Long.parseLong(response.header("Location").split("/")[2]);
	}

	private static ExtractableResponse<Response> 지하철_노선_생성_요청(String name, String color, Long upStationId,
		Long downStationId,
		int distance) {
		return RestAssured.given().log().all()
			.body(new LineRequest(name, color, upStationId, downStationId, distance))
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("/lines")
			.then().log().all()
			.extract();
	}

	public static ExtractableResponse<Response> 지하철_노선_생성_요청(Map<String, String> params) {
		String name = params.get("name");
		String color = params.get("color");
		Long upStationId = Long.parseLong(params.get("upStation"));
		Long downStationId = Long.parseLong(params.get("downStation"));
		int distance = Integer.parseInt(params.get("distance"));
		return 지하철_노선_생성_요청(name, color, upStationId, downStationId, distance);
	}
}
