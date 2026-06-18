import base64
import os
import zipfile

from docx import Document
from PIL import Image, ImageDraw, ImageFont


BASE = r"C:\Projects\NutritionApp\docs"
DOC_NAME = [
    f
    for f in os.listdir(BASE)
    if f.lower().endswith(".docx")
    and "пример" not in f.lower()
    and "попов" not in f.lower()
    and "эконом" not in f.lower()
][0]
DOC_PATH = os.path.join(BASE, DOC_NAME)
TMP_DOC = os.path.join(BASE, "__tmp_idef_bigtext.docx")
FINAL_DOC = os.path.join(BASE, "__final_idef_bigtext.docx")
FONT_REG = r"C:\Windows\Fonts\arial.ttf"
FONT_BOLD = r"C:\Windows\Fonts\arialbd.ttf"


def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size)


def wrap(draw, text, fnt, max_w):
    words = text.split()
    lines, cur = [], ""
    for word in words:
        test = (cur + " " + word).strip()
        if draw.textbbox((0, 0), test, font=fnt)[2] <= max_w or not cur:
            cur = test
        else:
            lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def lines(draw, x, y, text, fnt, fill, max_w, align="left", gap=5):
    for line in wrap(draw, text, fnt, max_w):
        bbox = draw.textbbox((0, 0), line, font=fnt)
        w = bbox[2] - bbox[0]
        if align == "center":
            tx = x - w / 2
        elif align == "right":
            tx = x - w
        else:
            tx = x
        draw.text((tx, y), line, font=fnt, fill=fill)
        y += (bbox[3] - bbox[1]) + gap
    return y


def text_block_size(draw, text, fnt, max_w, gap=5):
    wrapped = wrap(draw, text, fnt, max_w)
    widths, heights = [], []
    for line in wrapped:
        bbox = draw.textbbox((0, 0), line, font=fnt)
        widths.append(bbox[2] - bbox[0])
        heights.append(bbox[3] - bbox[1])
    return (max(widths) if widths else 0), sum(heights) + gap * max(0, len(heights) - 1), wrapped, heights


def text_panel(draw, x, y, text, max_w, size=26, align="left", bold=False, bg=False):
    fnt = font(size, bold)
    if bg:
        tw, th, _, _ = text_block_size(draw, text, fnt, max_w)
        x1 = x - tw / 2 if align == "center" else x - tw if align == "right" else x
        draw.rounded_rectangle((x1 - 8, y - 6, x1 + tw + 8, y + th + 8), radius=6, fill="#f7f8fc")
    return lines(draw, x, y, text, fnt, "#334155", max_w, align=align, gap=5)


def compact_label(draw, x, y, code, text, width=180, code_size=20, text_size=23, align="left"):
    f_code = font(code_size, True)
    f_text = font(text_size)
    text_w, text_h, _, _ = text_block_size(draw, text, f_text, width, gap=4)
    code_box = draw.textbbox((0, 0), code, font=f_code)
    code_w = code_box[2] - code_box[0]
    box_w = max(code_w, text_w)
    x1 = x - box_w / 2 if align == "center" else x - box_w if align == "right" else x
    draw.rounded_rectangle((x1 - 8, y - 6, x1 + box_w + 8, y + 34 + text_h + 8), radius=6, fill="#f7f8fc")
    draw.text((x, y), code, font=f_code, fill="#27364f")
    lines(draw, x, y + 30, text, f_text, "#334155", width, align=align, gap=4)


def leader(draw, start, end):
    draw.line([start, end], fill="#94a3b8", width=2)


def box(draw, rect, title, num, title_size=25):
    x1, y1, x2, y2 = rect
    draw.rectangle(rect, outline="#27364f", width=3, fill="#ffffff")
    lines(
        draw,
        (x1 + x2) / 2,
        y1 + (y2 - y1) / 2 - 24,
        title,
        font(title_size, True),
        "#1f2937",
        (x2 - x1) - 50,
        align="center",
        gap=5,
    )
    bbox = draw.textbbox((0, 0), num, font=font(19, True))
    draw.text(
        (x2 - (bbox[2] - bbox[0]) - 14, y2 - (bbox[3] - bbox[1]) - 10),
        num,
        font=font(19, True),
        fill="#27364f",
    )


def arrow(draw, pts, fill="#27364f", width=3, head=14):
    draw.line(pts, fill=fill, width=width)
    (x1, y1), (x2, y2) = pts[-2], pts[-1]
    if abs(x2 - x1) >= abs(y2 - y1):
        tri = (
            [(x2, y2), (x2 - head, y2 - head // 2), (x2 - head, y2 + head // 2)]
            if x2 >= x1
            else [(x2, y2), (x2 + head, y2 - head // 2), (x2 + head, y2 + head // 2)]
        )
    else:
        tri = (
            [(x2, y2), (x2 - head // 2, y2 - head), (x2 + head // 2, y2 - head)]
            if y2 >= y1
            else [(x2, y2), (x2 - head // 2, y2 + head), (x2 + head // 2, y2 + head)]
        )
    draw.polygon(tri, fill=fill)


def label(draw, x, y, code, text, width=230, code_size=22, text_size=26, align="left"):
    compact_label(draw, x, y, code, text, width, code_size, text_size, align)


def make_context(path):
    w, h = 2400, 1350
    img = Image.new("RGB", (w, h), "#f7f8fc")
    d = ImageDraw.Draw(img)
    d.text((w / 2, 42), "Контекстная IDEF0-диаграмма A-0", font=font(38, True), fill="#1f2937", anchor="ma")
    d.text(
        (w / 2, 95),
        "Обеспечить персонализированное планирование рациона питания",
        font=font(31, True),
        fill="#1f2937",
        anchor="ma",
    )

    rect = (785, 420, 1585, 745)
    box(d, rect, "Обеспечить персонализированное планирование рациона питания", "0", 24)

    arrow(d, [(115, 500), (785, 500)])
    text_panel(d, 135, 425, "Профиль пользователя", 220, 25, bg=True)
    arrow(d, [(115, 590), (785, 590)])
    text_panel(d, 135, 520, "История питания и дневник", 220, 25, bg=True)
    arrow(d, [(115, 680), (785, 680)])
    text_panel(d, 135, 625, "Выбранные продукты и холодильник", 230, 25, bg=True)

    for x, title, width in [
        (950, "Нормы физиологических потребностей [4]", 300),
        (1200, "Аллергии, цели и исключения", 300),
        (1450, "Правила рекомендаций", 260),
    ]:
        arrow(d, [(x, 285), (x, 420)])
        text_panel(d, x, 165, title, width, 25, align="center", bg=True)

    for y, title, width in [
        (500, "Рекомендации рецептов", 350),
        (590, "План питания", 260),
        (680, "Список покупок и аналитика", 360),
    ]:
        arrow(d, [(1585, y), (2190, y)])
        text_panel(d, 1660, y - 42, title, min(width, 280), 25, bg=True)

    for x, tx, title, width in [
        (950, 900, "Android-приложение", 190),
        (1200, 1160, "REST API и сервер", 170),
        (1450, 1410, "База данных и ML-модуль", 220),
    ]:
        arrow(d, [(x, 1180), (x, 745)])
        leader(d, (x, 1180), (tx + width / 2, 1205))
        text_panel(d, tx, 1210, title, width, 24, bg=True)
    img.save(path)


def make_a0(path):
    w, h = 2650, 1400
    img = Image.new("RGB", (w, h), "#f7f8fc")
    d = ImageDraw.Draw(img)
    d.text((w / 2, 42), "IDEF0-диаграмма узла A0", font=font(38, True), fill="#1f2937", anchor="ma")
    d.text(
        (w / 2, 96),
        "Поддержать полный цикл цифрового сопровождения питания пользователя",
        font=font(31, True),
        fill="#1f2937",
        anchor="ma",
    )

    b1 = (175, 300, 640, 500)
    b2 = (610, 610, 1075, 810)
    b3 = (1265, 850, 1730, 1050)
    b4 = (1830, 1130, 2295, 1330)
    for rect, title, num in [
        (b1, "Управлять профилем и ограничениями", "1"),
        (b2, "Вести и хранить данные питания", "2"),
        (b3, "Формировать рекомендации и план питания", "3"),
        (b4, "Представлять результаты пользователю", "4"),
    ]:
        box(d, rect, title, num)

    arrow(d, [(70, 390), (175, 390)])
    label(d, 45, 200, "I1", "Данные профиля", 170, text_size=24)
    arrow(d, [(70, 715), (610, 715)])
    label(d, 45, 555, "I2", "Записи дневника и продукты", 190, text_size=24)

    arrow(d, [(1410, 245), (1410, 850)])
    label(d, 1325, 135, "C1", "Нормы питания [4]", 170, text_size=24)
    arrow(d, [(1665, 245), (1665, 850)])
    label(d, 1580, 135, "C2", "Аллергии и цели", 180, text_size=24)

    arrow(d, [(2295, 1230), (2575, 1230)])
    label(d, 2385, 1065, "O1", "Рацион, рекомендации и покупки", 185, text_size=24)

    arrow(d, [(410, 1340), (410, 500)])
    leader(d, (410, 1340), (330, 1300))
    label(d, 285, 1275, "M1", "Android-клиент", 175, text_size=24)
    arrow(d, [(845, 1340), (845, 810)])
    leader(d, (845, 1340), (780, 1300))
    label(d, 735, 1275, "M2", "REST API", 135, text_size=24)
    arrow(d, [(1510, 1340), (1510, 1050)])
    leader(d, (1510, 1340), (1430, 1300))
    label(d, 1385, 1275, "M3", "База данных", 155, text_size=24)

    arrow(d, [(640, 400), (725, 400), (725, 610)])
    text_panel(d, 665, 325, "Профиль и ограничения", 260)
    arrow(d, [(1075, 710), (1285, 710), (1285, 850)])
    text_panel(d, 1105, 640, "Контекст питания", 250)
    arrow(d, [(1730, 960), (1875, 960), (1875, 1130)])
    text_panel(d, 1750, 890, "План и объяснения", 250)
    img.save(path)


def table_box(draw, rect, title, fields, fill="#ffffff"):
    x1, y1, x2, y2 = rect
    draw.rounded_rectangle(rect, radius=8, outline="#27364f", width=3, fill=fill)
    draw.rectangle((x1, y1, x2, y1 + 48), outline="#27364f", width=0, fill="#dbeafe")
    draw.text(((x1 + x2) / 2, y1 + 12), title, font=font(24, True), fill="#1f2937", anchor="ma")
    y = y1 + 65
    for field in fields:
        draw.text((x1 + 22, y), field, font=font(20), fill="#334155")
        y += 31


def rel(draw, start, end, label_text, offset=(0, 0)):
    arrow(draw, [start, end], width=3, head=13)
    x = (start[0] + end[0]) / 2 + offset[0]
    y = (start[1] + end[1]) / 2 + offset[1]
    bbox = draw.textbbox((0, 0), label_text, font=font(21, True))
    pad = 7
    draw.rounded_rectangle(
        (x - pad, y - pad, x + bbox[2] - bbox[0] + pad, y + bbox[3] - bbox[1] + pad),
        radius=6,
        fill="#f7f8fc",
        outline="#cbd5e1",
        width=1,
    )
    draw.text((x, y), label_text, font=font(21, True), fill="#27364f")


def rel_path(draw, pts, label_text, label_at, align="left"):
    arrow(draw, pts, width=3, head=13)
    x, y = label_at
    bbox = draw.textbbox((0, 0), label_text, font=font(20, True))
    pad = 7
    if align == "right":
        x = x - (bbox[2] - bbox[0])
    draw.rounded_rectangle(
        (x - pad, y - pad, x + bbox[2] - bbox[0] + pad, y + bbox[3] - bbox[1] + pad),
        radius=6,
        fill="#f7f8fc",
        outline="#cbd5e1",
        width=1,
    )
    draw.text((x, y), label_text, font=font(20, True), fill="#27364f")


def make_er(path):
    w, h = 2450, 1400
    img = Image.new("RGB", (w, h), "#f7f8fc")
    d = ImageDraw.Draw(img)
    d.text((w / 2, 42), "ER-диаграмма серверной базы данных", font=font(38, True), fill="#1f2937", anchor="ma")
    d.text(
        (w / 2, 96),
        "Основные сущности и отношения приложения питания",
        font=font(31, True),
        fill="#1f2937",
        anchor="ma",
    )

    boxes = {
        "users": (1025, 170, 1425, 390),
        "diary": (1025, 545, 1425, 800),
        "recipes": (170, 515, 570, 770),
        "products": (1880, 515, 2280, 770),
        "ingredients": (1025, 990, 1425, 1215),
        "pantry": (1880, 880, 2280, 1105),
        "prefs": (1880, 190, 2280, 415),
        "plans": (170, 185, 570, 410),
        "events": (170, 875, 570, 1120),
        "water_weight": (1480, 1100, 1880, 1310),
    }
    table_box(d, boxes["users"], "users", ["PK id", "email", "first_name", "weight, target_weight", "activity_level, goal"])
    table_box(d, boxes["diary"], "food_diary_entries", ["PK id", "FK user_id", "FK product_id?", "FK recipe_id?", "meal_type, date", "weight, calories, P/F/C"])
    table_box(d, boxes["recipes"], "recipes", ["PK id", "title", "calories, P/F/C", "category", "image_url"])
    table_box(d, boxes["products"], "products", ["PK id", "name", "barcode", "calories, P/F/C", "category"])
    table_box(d, boxes["ingredients"], "recipe_ingredients", ["PK id", "FK recipe_id", "FK product_id", "amount", "unit"])
    table_box(d, boxes["pantry"], "pantry_items", ["PK id", "FK user_id", "FK product_id", "amount", "expires_at"])
    table_box(d, boxes["prefs"], "food_preferences", ["PK id", "FK user_id", "FK product_id", "type: favorite/excluded", "reason"])
    table_box(d, boxes["plans"], "meal_plans", ["PK id", "FK user_id", "date_from, date_to", "status"])
    table_box(d, boxes["events"], "recommendation_events", ["PK id", "FK user_id", "FK recipe_id", "event_type", "score, created_at"])
    table_box(d, boxes["water_weight"], "water_and_weight_history", ["PK id", "FK user_id", "date", "water_ml", "weight"])

    # User-owned data.
    rel_path(d, [(1025, 350), (900, 350), (900, 665), (1025, 665)], "1:N diary", (925, 600))
    rel_path(d, [(1025, 250), (720, 250), (570, 250)], "1:N plans", (640, 215))
    rel_path(d, [(1025, 320), (720, 320), (720, 995), (570, 995)], "1:N events", (625, 930))
    rel_path(d, [(1425, 250), (1680, 250), (1880, 250)], "1:N prefs", (1590, 215))
    rel_path(d, [(1425, 330), (1710, 330), (1710, 985), (1880, 985)], "1:N pantry", (1590, 930))
    rel_path(d, [(1425, 365), (1510, 365), (1510, 1100)], "1:N history", (1530, 1005))

    # Diary can reference either a product or a recipe.
    rel_path(d, [(1025, 635), (570, 635)], "N:1 recipe", (650, 595))
    rel_path(d, [(1425, 635), (1880, 635)], "N:1 product", (1585, 595))

    # Recipe composition is a many-to-many relation through recipe_ingredients.
    rel_path(d, [(570, 705), (760, 705), (760, 1100), (1025, 1100)], "1:N", (795, 1060))
    rel_path(d, [(1425, 1100), (1685, 1100), (1685, 705), (1880, 705)], "N:1", (1635, 1060))
    d.text((960, 945), "M:N recipes-products реализуется через recipe_ingredients", font=font(22, True), fill="#27364f")

    # Product-backed personal collections.
    rel_path(d, [(2080, 415), (2080, 515)], "N:1 product", (2100, 455))
    rel_path(d, [(2080, 880), (2080, 770)], "N:1 product", (2100, 820))

    # Recommendation events point to recipes.
    rel_path(d, [(570, 1040), (860, 1040), (860, 705), (570, 705)], "N:1 recipe", (640, 990))

    d.text((80, 1290), "Обозначения: PK - первичный ключ, FK - внешний ключ, 1:N - один ко многим, M:N - многие ко многим.", font=font(22), fill="#475569")
    img.save(path)


def make_a3(path):
    w, h = 2550, 1450
    img = Image.new("RGB", (w, h), "#f7f8fc")
    d = ImageDraw.Draw(img)
    d.text((w / 2, 42), "IDEF0-диаграмма узла A3", font=font(38, True), fill="#1f2937", anchor="ma")
    d.text((w / 2, 96), "Формировать рекомендации и план питания", font=font(31, True), fill="#1f2937", anchor="ma")

    b1 = (165, 285, 620, 500)
    b2 = (650, 610, 1110, 825)
    b3 = (1240, 880, 1700, 1095)
    b4 = (1790, 1160, 2250, 1375)
    for rect, title, num in [
        (b1, "Собрать пользовательский контекст", "1"),
        (b2, "Отфильтровать недопустимые рецепты", "2"),
        (b3, "Рассчитать гибридный ранг рецептов", "3"),
        (b4, "Вернуть выдачу и сохранить обратную связь", "4"),
    ]:
        box(d, rect, title, num, 23)

    arrow(d, [(60, 360), (165, 360)])
    label(d, 40, 170, "I1", "Профиль и цели", 180, text_size=24)
    arrow(d, [(60, 445), (165, 445)])
    label(d, 40, 300, "I2", "История питания", 170, text_size=24)
    arrow(d, [(60, 715), (650, 715)])
    label(d, 40, 545, "I3", "Каталог рецептов и холодильник", 210, text_size=24)

    for x, title, width in [
        (1340, "Тип приема пищи и ограничения", 280),
        (1625, "Правила ранжирования", 250),
        (1920, "Параметры ML-модели", 250),
    ]:
        arrow(d, [(x, 250), (x, 880 if x != 1920 else 1160)])
        label(d, x - 80, 135, "C" + str([1340, 1625, 1920].index(x) + 1), title, min(width, 210), text_size=24)

    arrow(d, [(2250, 1250), (2470, 1250)])
    label(d, 2290, 1100, "O1", "Выдача", 135, text_size=24)
    arrow(d, [(2250, 1340), (2470, 1340)])
    label(d, 2290, 1290, "O2", "Показ и обратная связь", 170, text_size=24)

    arrow(d, [(395, 1410), (395, 500)])
    leader(d, (395, 1410), (280, 1370))
    label(d, 130, 1340, "M1", "Recommendation Service", 175, text_size=23)
    arrow(d, [(910, 1410), (910, 825)])
    leader(d, (910, 1410), (850, 1370))
    label(d, 810, 1340, "M2", "ML reranker", 145, text_size=23)
    arrow(d, [(1470, 1410), (1470, 1095)])
    leader(d, (1470, 1410), (1390, 1370))
    label(d, 1320, 1335, "M3", "База событий и рецептов", 190, text_size=23)

    arrow(d, [(620, 395), (760, 395), (760, 610)])
    text_panel(d, 650, 325, "Контекст пользователя", 270)
    arrow(d, [(1110, 715), (1290, 715), (1290, 880)])
    text_panel(d, 1140, 640, "Допустимые кандидаты", 270)
    arrow(d, [(1700, 990), (1850, 990), (1850, 1160)])
    text_panel(d, 1730, 920, "Выдача и объяснение", 270)
    img.save(path)


def png_to_svg(png_path, svg_path):
    with open(png_path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("ascii")
    img = Image.open(png_path)
    with open(svg_path, "w", encoding="utf-8") as f:
        f.write(
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{img.width}" height="{img.height}" viewBox="0 0 {img.width} {img.height}">'
            f'<image href="data:image/png;base64,{b64}" width="{img.width}" height="{img.height}"/>'
            f"</svg>"
        )


def main():
    p1 = os.path.join(BASE, "figure-1-1-context.png")
    p2 = os.path.join(BASE, "figure-1-2-scenarios.png")
    p3 = os.path.join(BASE, "figure-2-1-er-model.png")
    p4 = os.path.join(BASE, "ml-recommendation-scheme.png")
    make_context(p1)
    make_a0(p2)
    make_er(p3)
    make_a3(p4)

    png_to_svg(p1, os.path.join(BASE, "figure-1-1-context.svg"))
    png_to_svg(p2, os.path.join(BASE, "figure-1-2-scenarios.svg"))
    png_to_svg(p3, os.path.join(BASE, "figure-2-1-er-model.svg"))
    png_to_svg(p4, os.path.join(BASE, "ml-recommendation-scheme.svg"))

    Document(DOC_PATH).save(TMP_DOC)
    repl = {
        "word/media/image1.png": p1,
        "word/media/image2.png": p2,
        "word/media/image3.png": p3,
        "word/media/image7.png": p4,
    }
    with zipfile.ZipFile(TMP_DOC, "r") as zin, zipfile.ZipFile(FINAL_DOC, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if item.filename in repl:
                with open(repl[item.filename], "rb") as f:
                    zout.writestr(item, f.read())
            else:
                zout.writestr(item, zin.read(item.filename))
    os.replace(FINAL_DOC, DOC_PATH)

    if os.path.exists(TMP_DOC):
        os.remove(TMP_DOC)
    print("UPDATED", DOC_PATH)


if __name__ == "__main__":
    main()
