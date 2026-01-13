package com.weis.demo.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegDocumentSplitter 测试类
 */
class RegDocumentSplitterTest {

    private RegDocumentSplitter splitter;

    @BeforeEach
    void setUp() {
        // 使用指定的正则表达式初始化
        splitter = new RegDocumentSplitter("===Slice Start===(.*?)===Slice End===");
    }

    @Test
    void testSplitDocumentWithMultipleSlices() {
        // 准备测试数据
        String text = """
                这是文档的开头部分。
                ===Slice Start===
                第一个切片的内容，包含了一些重要信息。
                这是第一个切片的第二行。
                ===Slice End===
                这是两个切片之间的内容。
                ===Slice Start===
                第二个切片的内容。
                包含更多数据。
                ===Slice End===
                这是文档的结尾部分。
                """;

        Document document = Document.from(text);

        // 执行分割
        List<TextSegment> segments = splitter.split(document);

        // 验证结果
        assertNotNull(segments);
        assertEquals(2, segments.size(), "应该提取出2个切片内容");

        // 验证每个段落的内容（只包含切片标记中间的内容）
        assertTrue(segments.get(0).text().contains("第一个切片的内容"));
        assertTrue(segments.get(0).text().contains("这是第一个切片的第二行"));
        assertTrue(segments.get(1).text().contains("第二个切片的内容"));
        assertTrue(segments.get(1).text().contains("包含更多数据"));
    }

    @Test
    void testSplitDocumentWithMetadata() {
        // 准备带元数据的文档
        String text = """
                开头内容
                ===Slice Start===
                切片内容
                ===Slice End===
                结尾内容
                """;

        Metadata metadata = new Metadata();
        metadata.put("source", "test-document");
        metadata.put("author", "test-author");

        Document document = Document.from(text, metadata);

        // 执行分割
        List<TextSegment> segments = splitter.split(document);

        // 验证元数据被保留
        assertNotNull(segments);
        assertTrue(segments.size() > 0);
        for (TextSegment segment : segments) {
            assertEquals("test-document", segment.metadata().getString("source"));
            assertEquals("test-author", segment.metadata().getString("author"));
        }
    }

    @Test
    void testSplitEmptyDocument() {
        Document document = Document.from("");
        List<TextSegment> segments = splitter.split(document);

        assertNotNull(segments);
        assertEquals(0, segments.size(), "空文档应该返回空列表");
    }

    @Test
    void testSplitNullDocument() {
        List<TextSegment> segments = splitter.split(null);

        assertNotNull(segments);
        assertEquals(0, segments.size(), "null文档应该返回空列表");
    }

    @Test
    void testSplitDocumentWithoutSliceMarkers() {
        String text = "这是一个不包含任何切片标记的普通文档。";
        Document document = Document.from(text);

        List<TextSegment> segments = splitter.split(document);

        assertNotNull(segments);
        assertEquals(0, segments.size(), "没有切片标记的文档应该返回空列表");
    }

    @Test
    void testSplitDocumentWithOnlySliceMarkers() {
        String text = """
                ===Slice Start===
                切片内容1
                ===Slice End===
                ===Slice Start===
                切片内容2
                ===Slice End===
                """;

        Document document = Document.from(text);
        List<TextSegment> segments = splitter.split(document);

        assertNotNull(segments);
        assertEquals(2, segments.size(), "应该提取出2个切片内容");
        assertTrue(segments.get(0).text().contains("切片内容1"));
        assertTrue(segments.get(1).text().contains("切片内容2"));
    }

    @Test
    void testSplitDocumentWithWhitespaceOnly() {
        String text = """
                   
                ===Slice Start===
                
                ===Slice End===
                   
                """;

        Document document = Document.from(text);
        List<TextSegment> segments = splitter.split(document);

        assertNotNull(segments);
        // 空白段落应该被过滤掉
        assertEquals(0, segments.size(), "只包含空白的段落应该被过滤");
    }

    @Test
    void testSplitDocumentWithChineseContent() {
        String text = """
                中文内容测试
                ===Slice Start===
                这是中文切片内容，包含各种标点符号：，。！？、；：""''
                ===Slice End===
                更多中文内容
                """;

        Document document = Document.from(text);
        List<TextSegment> segments = splitter.split(document);

        assertNotNull(segments);
        assertEquals(1, segments.size(), "应该提取出1个切片内容");
        assertTrue(segments.get(0).text().contains("这是中文切片内容"));
        assertTrue(segments.get(0).text().contains("标点符号"));
    }

    @Test
    void testDifferentRegexPattern() {
        // 测试使用不同的正则表达式
        RegDocumentSplitter newLineSplitter = new RegDocumentSplitter("\\n\\n+");

        String text = """
                第一段内容
                
                第二段内容
                
                
                第三段内容
                """;

        Document document = Document.from(text);
        List<TextSegment> segments = newLineSplitter.split(document);

        assertNotNull(segments);
        assertEquals(3, segments.size(), "按双换行符分割应该得到3个段落");
    }
}
