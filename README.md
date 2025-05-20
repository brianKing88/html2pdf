# html2pdf

这是一个基于 Java 和 iText 7 的工具，用于将 HTML 文件转换为 PDF 文档。

## 功能

*   将本地 HTML 文件转换为 PDF。
*   支持通过指定字体目录来使用自定义字体 (例如：微软雅黑、黑体、宋体等)。
*   自动处理 HTML 中的特殊字符实体 (如 `&nbsp;`)。
*   自动在生成的 PDF 中添加页码 ("第 X 页 / 共 Y 页")。
*   提供了页眉、页脚和水印的事件处理器基础代码 (在 `com.example.pdf` 包下，可根据需要集成到转换流程中)。
*   可以通过参数设置PDF的页面大小 (默认为 A4)。

## 技术栈

*   Java (JDK 8+)
*   iText 7 (`com.itextpdf:html2pdf`, `com.itextpdf:font-asian`)
*   Maven (用于项目构建和依赖管理)
*   JUnit (用于单元测试)

## 如何构建和运行

### 前提条件

*   Java Development Kit (JDK) 8 或更高版本安装并配置。
*   Apache Maven 安装并配置。

### 构建项目

在项目根目录下打开终端，运行以下命令来编译项目并打包成 JAR 文件：

```bash
mvn clean package
```

或者，如果想将构件安装到本地 Maven 仓库：

```bash
mvn clean install
```

### 运行测试

项目包含 JUnit 测试，以验证核心转换功能。可以通过以下任一方式运行测试：

1.  **使用 Maven:**

    ```bash
    mvn test
    ```
2.  **使用提供的 Shell 脚本 (推荐):**

    首先，确保脚本有执行权限：
    ```bash
    chmod +x run_tests.sh
    ```
    然后运行脚本：
    ```bash
    ./run_tests.sh
    ```
    测试成功后，生成的示例 PDF 文件会保存在项目根目录下的 `test_outputs` 文件夹中。

## 如何使用

核心转换逻辑位于 `com.example.pdf.HtmlToPdfConverter` 类中的静态方法 `convertToPdf`。

```java
import com.example.pdf.HtmlToPdfConverter;
import com.itextpdf.kernel.geom.PageSize;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) {
        try {
            File htmlInputFile = new File("path/to/your/input.html"); // 替换为你的HTML文件路径
            File pdfOutputFile = new File("path/to/your/output.pdf");   // 替换为你想要的PDF输出路径
            String fontDir = "src/fonts"; // 指向包含自定义字体的目录 (例如 .ttf, .otf 文件)
            // 如果不需要自定义字体目录，可以传递 null，将使用iText默认字体或标准回退字体

            try (OutputStream os = new FileOutputStream(pdfOutputFile)) {
                HtmlToPdfConverter.convertToPdf(htmlInputFile, fontDir, os, PageSize.A4);
                System.out.println("PDF created successfully at: " + pdfOutputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**参数说明:**

*   `File file`: 要转换的源 HTML 文件。
*   `String customFontDir`: 包含自定义字体文件（如 `.ttf`, `.otf`）的目录路径。如果为 `null` 或无效路径，将尝试使用标准回退字体 (如 STSongStd-Light)。
*   `OutputStream outputStream`: PDF 内容将写入此输出流。
*   `PageSize pageSize`: 生成 PDF 的页面大小 (例如 `PageSize.A4`, `PageSize.LETTER`)。如果为 `null`，默认为 `PageSize.A4`。

## 字体注意事项

*   **添加自定义字体**: 将您的字体文件 (如 `msyh.ttf` for 微软雅黑, `simhei.ttf` for 黑体) 放入一个目录中，例如项目中的 `src/fonts` 目录，然后在调用 `convertToPdf` 时传递此目录的路径。
*   **HTML 中的字体声明**: 在您的 HTML 中，通过 CSS `font-family` 属性指定字体，例如：
    ```html
    <p style="font-family: 'Microsoft YaHei', 微软雅黑;">这是微软雅黑字体。</p>
    <p style="font-family: SimHei, 黑体;">这是黑体字体。</p>
    <p style="font-family: SimSun, 宋体;">这是宋体字体。</p>
    ```
    iText 会尝试将这些 `font-family` 名称与加载的字体文件内部定义的字体名称进行匹配。
*   **字体格式**: iText 通常支持 TrueType (`.ttf`) 和 OpenType (`.otf`) 字体。

## 已知问题 / 注意事项

*   **SLF4J 日志警告**: 在运行测试或程序时，您可能会看到类似 `SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"` 的警告。这是因为 SLF4J (iText 使用的日志门面) 没有找到具体的日志实现绑定。这通常不影响程序功能，但如果您希望启用详细的日志记录，可以在 `pom.xml` 的 `<dependencies>` 部分添加一个 SLF4J 实现，例如 `slf4j-simple`：
    ```xml
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.32</version> <!-- 或更新版本 -->
        <scope>runtime</scope> <!-- 或 test，取决于您的需要 -->
    </dependency>
    ```
*   **HTML 规范性**: 输入的 HTML 文件的规范性和简洁性会影响转换结果的质量和成功率。高度复杂、包含脚本或格式不正确的 HTML 可能会导致转换问题或与预期不符的输出。建议使用结构良好、样式主要通过标准 CSS 定义的 HTML。
*   **CSS 支持**: iText 的 HTML 转 PDF 功能对 CSS 的支持有一定限制，并非所有现代 CSS3 属性都能完美呈现。对于复杂的布局和样式，可能需要进行调整或简化。
*   **Linter 错误修复**: 如果您使用了 `test.html` 并遇到问题，请确保修复其中由 `<!-- Linter Errors -->` 标记的 CSS 错误 (例如，空的 `background-color:;` 属性应修正或移除)。
