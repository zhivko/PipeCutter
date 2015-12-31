Sub Sketch2Face2()
    Dim oApp As Application
    Set oApp = ThisApplication
    Dim oPart As PartDocument
    Set oPart = oApp.ActiveDocument
    Dim oCompDef As ComponentDefinition
    Set oCompDef = oPart.ComponentDefinition
    Dim oFaces As Faces
    Dim oFace As Face
    Dim oEdge As Edge
    Dim x1 As Double
    Dim y1 As Double
    Dim z1 As Double
    Dim x2 As Double
    Dim y2 As Double
    Dim z2 As Double
    Dim cx As Double
    Dim cy As Double
    Dim cz As Double
    
    Dim p As Point
    Dim l As Long
    
    Dim pline As Polyline3d
    
    Dim points() As Double
    Dim edgeNo As Integer
    Dim vertexCoordinates() As Double
    
    Dim vertexIndices() As Long
    
    Open "c:\Users\klemen\Dropbox\TerasaBoris\grbl\data.csv" For Output As #1
    
    Dim objUOM As UnitsOfMeasure
    Dim oCOM As Point
    
    Dim oDoc As Inventor.Document
    Dim oWS As WorkSurface
    
    Set objUOM = oPart.UnitsOfMeasure
    Set oCOM = oCompDef.MassProperties.CenterOfMass
    
    maxX = oCompDef.RangeBox.MaxPoint.X
    maxY = oCompDef.RangeBox.MaxPoint.Y
    maxZ = oCompDef.RangeBox.MaxPoint.Z
    
    minX = oCompDef.RangeBox.MinPoint.X
    minY = oCompDef.RangeBox.MinPoint.Y
    minZ = oCompDef.RangeBox.MinPoint.Z

    centerX = (maxX + minX) / 2
    centerY = (maxY + minY) / 2
    centerZ = (maxZ + minZ) / 2
    
    cogx = objUOM.GetStringFromValue(oCOM.X, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogy = objUOM.GetStringFromValue(oCOM.Y, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogz = objUOM.GetStringFromValue(oCOM.Z, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    
    'This can be changed to .SideFaces, .EndFaces, or .StartFaces
    'But .Faces will let you choose between all faces
    Set oFaces = oCompDef.SurfaceBodies.Item(1).Faces
    edgeNo = 0
    Set oFaces = oCompDef.SurfaceBodies.Item(1).Faces
    
    surfaceNo = 0
    pointNo = 0
    
    'oAssyDoc = ThisApplication.ActiveDocument
    Set oDoc = ThisApplication.ActiveDocument
    
    
    'For Each oDoc In oAssyDoc.AllReferencedDocuments
        'set surface bodies visibility
        For Each oFace In oDoc.ComponentDefinition.SurfaceBodies.Item(1).Faces

            surfaceNo = surfaceNo + 1
            For Each oEdge In oFace.Edges
                
                'Debug.Print oEdge.GeometryForm & " " & oEdge.GeometryType
                
                If oEdge.GeometryType = kBSplineCurve Then
                    Debug.Print "kLineSegmentCurve"
                ElseIf oEdge.GeometryType = kCircleCurve Then
                    'Debug.Print "kCircleCurve"
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                ElseIf oEdge.GeometryType = kPolylineCurve Then
                
                Else
                    'Debug.Print oEdge.GeometryType
                End If
                
                
                Debug.Print oEdge.Type
                
                If edgeNo = 199 Or edgeNo = 200 Then
                    Debug.Print oEdge.TangentiallyConnectedEdges.Count
                    Debug.Print oEdge.Parent.AppearanceSourceType
                    For k = 1 To oEdge.EdgeUses.Count
                        Debug.Print oEdge.EdgeUses.Item(k).GeometryForm
                    Next k
                    Debug.Print oEdge.TransientKey
                    
                    
': AppearanceSourceType: kPartAppearance: AppearanceSourceTypeEnum: Module1.Sketch2Face2
                End If
                Debug.Print oEdge.TransientKey
                
                edgeNo = edgeNo + 1
                
                Set pline = ThisApplication.TransientGeometry.CreatePolyline3dFromCurve(oEdge.Geometry, 0.005)
                pointsStr = ""
                For l = 1 To pline.PointCount
                  pointNo = pointNo + 1
                  Set p = pline.PointAtIndex(l)
                  pointsStr = pointsStr & (p.X - centerX) & ";" & (p.Y - centerY) & ";" & (p.Z - centerZ) & ";"
                Next l
                pointsStr = Left(pointsStr, Len(pointsStr) - 1)
                Print #1, "POLY_" & Trim(Str(surfaceNo)) & "_" & Trim(Str(edgeNo)) & "_" & pointNo & ";" & pointsStr
            
            Next
        Next
    'Next
    Close #1
    
End Sub